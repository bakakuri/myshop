-- ============================================================
-- MYSHOP — Migration 003: Server-Side Functions & RPCs
-- ============================================================

-- ============================================================
-- ORDER NUMBER GENERATOR
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 1000;

CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TEXT AS $$
DECLARE
  prefix TEXT;
  seq_val INTEGER;
BEGIN
  SELECT COALESCE(value, 'ORD') INTO prefix
  FROM store_settings
  WHERE namespace = 'order' AND key = 'number_prefix';

  seq_val := nextval('order_number_seq');
  RETURN prefix || '-' || LPAD(seq_val::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- ATOMIC ORDER CREATION RPC
-- ============================================================
-- Called by Android client. Validates everything server-side.
-- Never trusts client-submitted prices or totals.

CREATE OR REPLACE FUNCTION create_order(
  p_customer_name TEXT,
  p_customer_phone TEXT,
  p_customer_email TEXT DEFAULT NULL,
  p_delivery_address TEXT DEFAULT NULL,
  p_delivery_city TEXT DEFAULT NULL,
  p_delivery_notes TEXT DEFAULT NULL,
  p_customer_note TEXT DEFAULT NULL,
  p_idempotency_key TEXT DEFAULT NULL,
  p_items JSONB DEFAULT '[]' -- [{product_id, variant_id, quantity}]
)
RETURNS JSONB AS $$
DECLARE
  v_user_id UUID;
  v_order_id UUID;
  v_conversation_id UUID;
  v_order_number TEXT;
  v_subtotal NUMERIC(10,2) := 0;
  v_total NUMERIC(10,2) := 0;
  v_item JSONB;
  v_product RECORD;
  v_variant RECORD;
  v_unit_price NUMERIC(10,2);
  v_line_total NUMERIC(10,2);
  v_currency TEXT;
  existing_order JSONB;
BEGIN
  v_user_id := auth.uid();

  -- Must be authenticated
  IF v_user_id IS NULL THEN
    RAISE EXCEPTION 'UNAUTHORIZED: Must be logged in to place an order';
  END IF;

  -- Check idempotency: if same key exists, return existing order
  IF p_idempotency_key IS NOT NULL THEN
    SELECT jsonb_build_object('order_id', id, 'order_number', order_number, 'conversation_id',
      (SELECT id FROM conversations WHERE order_id = orders.id LIMIT 1))
    INTO existing_order
    FROM orders
    WHERE idempotency_key = p_idempotency_key AND user_id = v_user_id;

    IF existing_order IS NOT NULL THEN
      RETURN jsonb_build_object('success', true, 'idempotent', true) || existing_order;
    END IF;
  END IF;

  -- Validate items array
  IF jsonb_array_length(p_items) = 0 THEN
    RAISE EXCEPTION 'INVALID: Cart is empty';
  END IF;

  -- Get currency setting
  SELECT COALESCE(value, 'GEL') INTO v_currency
  FROM store_settings WHERE namespace = 'catalog' AND key = 'currency';

  -- Generate order number
  v_order_number := generate_order_number();

  -- Create order record first
  INSERT INTO orders (
    order_number, user_id, status,
    customer_name, customer_phone, customer_email,
    delivery_address, delivery_city, delivery_notes, customer_note,
    subtotal, total, currency, idempotency_key
  ) VALUES (
    v_order_number, v_user_id, 'new',
    p_customer_name, p_customer_phone, p_customer_email,
    p_delivery_address, p_delivery_city, p_delivery_notes, p_customer_note,
    0, 0, v_currency, p_idempotency_key
  ) RETURNING id INTO v_order_id;

  -- Process each item
  FOR v_item IN SELECT * FROM jsonb_array_elements(p_items)
  LOOP
    -- Fetch product (must be published and available)
    SELECT * INTO v_product
    FROM products
    WHERE id = (v_item->>'product_id')::UUID
    AND is_published = TRUE
    AND is_available = TRUE
    FOR UPDATE; -- lock row

    IF NOT FOUND THEN
      RAISE EXCEPTION 'PRODUCT_UNAVAILABLE: Product % is not available',
        v_item->>'product_id';
    END IF;

    -- Check stock
    IF v_product.stock_quantity < (v_item->>'quantity')::INTEGER THEN
      RAISE EXCEPTION 'OUT_OF_STOCK: Insufficient stock for %', v_product.name_ka;
    END IF;

    -- Determine price (server-side, never client)
    v_unit_price := v_product.price;

    -- Handle variant if specified
    IF v_item->>'variant_id' IS NOT NULL THEN
      SELECT * INTO v_variant
      FROM product_variants
      WHERE id = (v_item->>'variant_id')::UUID
      AND product_id = v_product.id
      AND is_available = TRUE;

      IF NOT FOUND THEN
        RAISE EXCEPTION 'VARIANT_UNAVAILABLE: Variant not available';
      END IF;

      v_unit_price := v_unit_price + COALESCE(v_variant.price_adjustment, 0);
    END IF;

    v_line_total := v_unit_price * (v_item->>'quantity')::INTEGER;
    v_subtotal := v_subtotal + v_line_total;

    -- Insert order item with price snapshot
    INSERT INTO order_items (
      order_id, product_id, variant_id,
      product_name_ka, product_sku, variant_name_ka,
      unit_price, previous_price, quantity, line_total, image_url
    ) VALUES (
      v_order_id,
      v_product.id,
      CASE WHEN v_item->>'variant_id' IS NOT NULL THEN (v_item->>'variant_id')::UUID ELSE NULL END,
      v_product.name_ka,
      v_product.sku,
      CASE WHEN v_variant IS NOT NULL THEN v_variant.name_ka ELSE NULL END,
      v_unit_price,
      v_product.previous_price,
      (v_item->>'quantity')::INTEGER,
      v_line_total,
      v_product.main_image_url
    );

    -- Decrement stock
    UPDATE products
    SET stock_quantity = stock_quantity - (v_item->>'quantity')::INTEGER
    WHERE id = v_product.id;

  END LOOP;

  -- Update order totals (server-calculated)
  v_total := v_subtotal; -- No online payment, delivery agreed in chat
  UPDATE orders SET subtotal = v_subtotal, total = v_total WHERE id = v_order_id;

  -- Create initial status history
  INSERT INTO order_status_history (order_id, status, note, changed_by)
  VALUES (v_order_id, 'new', 'შეკვეთა მიღებულია', v_user_id);

  -- Create private conversation
  INSERT INTO conversations (order_id)
  VALUES (v_order_id)
  RETURNING id INTO v_conversation_id;

  -- Add customer as participant
  INSERT INTO conversation_participants (conversation_id, user_id, is_staff)
  VALUES (v_conversation_id, v_user_id, FALSE);

  -- Add all active support staff as participants
  INSERT INTO conversation_participants (conversation_id, user_id, is_staff)
  SELECT v_conversation_id, user_id, TRUE
  FROM staff_roles
  WHERE is_active = TRUE
  AND role IN ('owner', 'admin', 'order_manager', 'support_agent');

  -- Create welcome message from system
  INSERT INTO messages (conversation_id, sender_id, sender_type, content)
  VALUES (
    v_conversation_id,
    v_user_id,
    'customer',
    'შეკვეთა #' || v_order_number || ' გაგზავნილია. ადმინისტრატორი მალე დაგიკავშირდებათ.'
  );

  -- Create notification for customer
  INSERT INTO notifications (user_id, type, title_ka, body_ka, related_order_id, related_conversation_id)
  VALUES (
    v_user_id,
    'order_new',
    'შეკვეთა მიღებულია',
    'თქვენი შეკვეთა #' || v_order_number || ' წარმატებით გაიგზავნა.',
    v_order_id,
    v_conversation_id
  );

  -- Create notifications for all active staff
  INSERT INTO notifications (user_id, type, title_ka, body_ka, related_order_id)
  SELECT user_id, 'order_new', 'ახალი შეკვეთა', 'შეკვეთა #' || v_order_number || ' შემოვიდა.', v_order_id
  FROM staff_roles WHERE is_active = TRUE;

  -- Audit log
  INSERT INTO audit_logs (actor_id, actor_role, action, resource_type, resource_id, new_value)
  VALUES (
    v_user_id, 'customer', 'order.create', 'order', v_order_id::TEXT,
    jsonb_build_object('order_number', v_order_number, 'total', v_total)
  );

  RETURN jsonb_build_object(
    'success', true,
    'order_id', v_order_id,
    'order_number', v_order_number,
    'conversation_id', v_conversation_id,
    'total', v_total,
    'currency', v_currency
  );

EXCEPTION
  WHEN OTHERS THEN
    -- Rollback happens automatically
    RAISE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- UPDATE ORDER STATUS (Admin only)
-- ============================================================

CREATE OR REPLACE FUNCTION admin_update_order_status(
  p_order_id UUID,
  p_new_status order_status_type,
  p_note TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
  v_actor_id UUID;
  v_old_status order_status_type;
  v_customer_id UUID;
  v_order_number TEXT;
BEGIN
  v_actor_id := auth.uid();

  IF NOT can_manage_orders() THEN
    RAISE EXCEPTION 'FORBIDDEN: Insufficient permissions';
  END IF;

  -- Get current state
  SELECT status, user_id, order_number
  INTO v_old_status, v_customer_id, v_order_number
  FROM orders WHERE id = p_order_id FOR UPDATE;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'NOT_FOUND: Order not found';
  END IF;

  -- Update order
  UPDATE orders SET status = p_new_status, updated_at = now()
  WHERE id = p_order_id;

  -- Append status history
  INSERT INTO order_status_history (order_id, status, note, changed_by)
  VALUES (p_order_id, p_new_status, p_note, v_actor_id);

  -- Notify customer
  INSERT INTO notifications (user_id, type, title_ka, body_ka, related_order_id)
  VALUES (
    v_customer_id,
    'order_status_update',
    'შეკვეთის სტატუსი განახლდა',
    'შეკვეთა #' || v_order_number || ': ' || p_new_status::TEXT,
    p_order_id
  );

  -- Audit
  INSERT INTO audit_logs (actor_id, action, resource_type, resource_id, old_value, new_value)
  VALUES (
    v_actor_id, 'order.status_update', 'order', p_order_id::TEXT,
    jsonb_build_object('status', v_old_status),
    jsonb_build_object('status', p_new_status, 'note', p_note)
  );

  RETURN jsonb_build_object('success', true, 'old_status', v_old_status, 'new_status', p_new_status);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- GET OR CREATE CART
-- ============================================================

CREATE OR REPLACE FUNCTION get_or_create_cart()
RETURNS UUID AS $$
DECLARE
  v_cart_id UUID;
  v_user_id UUID;
BEGIN
  v_user_id := auth.uid();
  IF v_user_id IS NULL THEN RAISE EXCEPTION 'UNAUTHORIZED'; END IF;

  SELECT id INTO v_cart_id FROM carts WHERE user_id = v_user_id;

  IF v_cart_id IS NULL THEN
    INSERT INTO carts (user_id) VALUES (v_user_id) RETURNING id INTO v_cart_id;
  END IF;

  RETURN v_cart_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- UPSERT CART ITEM
-- ============================================================

CREATE OR REPLACE FUNCTION upsert_cart_item(
  p_product_id UUID,
  p_quantity INTEGER,
  p_variant_id UUID DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
  v_cart_id UUID;
  v_product RECORD;
BEGIN
  -- Validate product is available
  SELECT * INTO v_product
  FROM products
  WHERE id = p_product_id AND is_published = TRUE AND is_available = TRUE;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'PRODUCT_UNAVAILABLE';
  END IF;

  v_cart_id := get_or_create_cart();

  IF p_quantity <= 0 THEN
    DELETE FROM cart_items
    WHERE cart_id = v_cart_id AND product_id = p_product_id
    AND (variant_id = p_variant_id OR (variant_id IS NULL AND p_variant_id IS NULL));
    RETURN jsonb_build_object('action', 'removed');
  END IF;

  INSERT INTO cart_items (cart_id, product_id, variant_id, quantity)
  VALUES (v_cart_id, p_product_id, p_variant_id, p_quantity)
  ON CONFLICT (cart_id, product_id, variant_id)
    DO UPDATE SET quantity = EXCLUDED.quantity;

  RETURN jsonb_build_object('action', 'upserted', 'cart_id', v_cart_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- TOGGLE FAVORITE
-- ============================================================

CREATE OR REPLACE FUNCTION toggle_favorite(p_product_id UUID)
RETURNS JSONB AS $$
DECLARE
  v_user_id UUID := auth.uid();
  v_exists BOOLEAN;
BEGIN
  IF v_user_id IS NULL THEN RAISE EXCEPTION 'UNAUTHORIZED'; END IF;

  SELECT EXISTS(
    SELECT 1 FROM favorites WHERE user_id = v_user_id AND product_id = p_product_id
  ) INTO v_exists;

  IF v_exists THEN
    DELETE FROM favorites WHERE user_id = v_user_id AND product_id = p_product_id;
    RETURN jsonb_build_object('favorited', false);
  ELSE
    INSERT INTO favorites (user_id, product_id) VALUES (v_user_id, p_product_id)
    ON CONFLICT DO NOTHING;
    RETURN jsonb_build_object('favorited', true);
  END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- MARK MESSAGES READ
-- ============================================================

CREATE OR REPLACE FUNCTION mark_conversation_read(p_conversation_id UUID)
RETURNS VOID AS $$
DECLARE
  v_user_id UUID := auth.uid();
BEGIN
  IF NOT is_conversation_participant(p_conversation_id) AND NOT can_manage_conversations() THEN
    RAISE EXCEPTION 'FORBIDDEN';
  END IF;

  UPDATE messages
  SET is_read = TRUE, read_at = now()
  WHERE conversation_id = p_conversation_id
  AND sender_id != v_user_id
  AND is_read = FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- ADMIN UPSERT SETTING
-- ============================================================

CREATE OR REPLACE FUNCTION admin_upsert_setting(
  p_namespace TEXT,
  p_key TEXT,
  p_value TEXT DEFAULT NULL,
  p_value_json JSONB DEFAULT NULL
)
RETURNS VOID AS $$
DECLARE
  v_actor_id UUID := auth.uid();
  v_old_value TEXT;
BEGIN
  IF NOT is_admin_or_owner() THEN
    RAISE EXCEPTION 'FORBIDDEN: Only admins can change settings';
  END IF;

  SELECT value INTO v_old_value
  FROM store_settings WHERE namespace = p_namespace AND key = p_key;

  INSERT INTO store_settings (namespace, key, value, value_json, updated_by, updated_at)
  VALUES (p_namespace, p_key, p_value, p_value_json, v_actor_id, now())
  ON CONFLICT (namespace, key)
  DO UPDATE SET
    value = EXCLUDED.value,
    value_json = EXCLUDED.value_json,
    updated_by = EXCLUDED.updated_by,
    updated_at = now();

  INSERT INTO audit_logs (actor_id, action, resource_type, resource_id, old_value, new_value)
  VALUES (
    v_actor_id, 'setting.update', 'store_setting', p_namespace || '.' || p_key,
    jsonb_build_object('value', v_old_value),
    jsonb_build_object('value', p_value)
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- INVITE STAFF MEMBER
-- ============================================================

CREATE OR REPLACE FUNCTION admin_assign_staff_role(
  p_user_id UUID,
  p_role user_role_type
)
RETURNS VOID AS $$
DECLARE
  v_actor_id UUID := auth.uid();
BEGIN
  IF NOT is_owner() THEN
    RAISE EXCEPTION 'FORBIDDEN: Only the owner can assign staff roles';
  END IF;

  -- Cannot assign owner role (only via direct DB migration)
  IF p_role = 'owner' THEN
    RAISE EXCEPTION 'FORBIDDEN: Cannot assign owner role via API';
  END IF;

  INSERT INTO staff_roles (user_id, role, is_active, granted_by)
  VALUES (p_user_id, p_role, TRUE, v_actor_id)
  ON CONFLICT (user_id)
  DO UPDATE SET role = EXCLUDED.role, is_active = TRUE, granted_by = EXCLUDED.granted_by;

  INSERT INTO audit_logs (actor_id, action, resource_type, resource_id, new_value)
  VALUES (v_actor_id, 'staff.assign_role', 'staff_role', p_user_id::TEXT,
    jsonb_build_object('role', p_role));
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- GET CART WITH CURRENT PRICES (server-validated)
-- ============================================================

CREATE OR REPLACE FUNCTION get_cart_summary()
RETURNS JSONB AS $$
DECLARE
  v_user_id UUID := auth.uid();
  v_cart_id UUID;
  v_items JSONB;
  v_subtotal NUMERIC(10,2);
BEGIN
  IF v_user_id IS NULL THEN RAISE EXCEPTION 'UNAUTHORIZED'; END IF;

  SELECT id INTO v_cart_id FROM carts WHERE user_id = v_user_id;
  IF v_cart_id IS NULL THEN
    RETURN jsonb_build_object('items', '[]'::JSONB, 'subtotal', 0, 'item_count', 0);
  END IF;

  SELECT
    jsonb_agg(
      jsonb_build_object(
        'cart_item_id', ci.id,
        'product_id', p.id,
        'product_name_ka', p.name_ka,
        'sku', p.sku,
        'unit_price', p.price,
        'previous_price', p.previous_price,
        'discount_percent', p.discount_percent,
        'quantity', ci.quantity,
        'line_total', p.price * ci.quantity,
        'image_url', p.main_image_url,
        'is_available', p.is_available AND p.is_published,
        'in_stock', p.stock_quantity >= ci.quantity,
        'variant_id', ci.variant_id,
        'variant_name_ka', pv.name_ka
      )
    ),
    SUM(p.price * ci.quantity)
  INTO v_items, v_subtotal
  FROM cart_items ci
  JOIN products p ON p.id = ci.product_id
  LEFT JOIN product_variants pv ON pv.id = ci.variant_id
  WHERE ci.cart_id = v_cart_id;

  RETURN jsonb_build_object(
    'cart_id', v_cart_id,
    'items', COALESCE(v_items, '[]'::JSONB),
    'subtotal', COALESCE(v_subtotal, 0),
    'item_count', (SELECT COUNT(*) FROM cart_items WHERE cart_id = v_cart_id)
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER STABLE;
