-- ============================================================
-- MYSHOP — Migration 002: Row Level Security Policies
-- ============================================================

-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE staff_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE role_permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_attributes ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_variants ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE related_products ENABLE ROW LEVEL SECURITY;
ALTER TABLE favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE carts ENABLE ROW LEVEL SECURITY;
ALTER TABLE cart_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_status_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversation_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE message_attachments ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE banners ENABLE ROW LEVEL SECURITY;
ALTER TABLE home_sections ENABLE ROW LEVEL SECURITY;
ALTER TABLE home_section_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_pages ENABLE ROW LEVEL SECURITY;
ALTER TABLE saved_replies ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- HELPER FUNCTIONS
-- ============================================================

-- Check if the calling user is staff
CREATE OR REPLACE FUNCTION is_staff()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM staff_roles
    WHERE user_id = auth.uid()
    AND is_active = TRUE
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

-- Check if the calling user has a specific role
CREATE OR REPLACE FUNCTION has_role(r user_role_type)
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM staff_roles
    WHERE user_id = auth.uid()
    AND role = r
    AND is_active = TRUE
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

-- Check if the calling user is owner or admin
CREATE OR REPLACE FUNCTION is_admin_or_owner()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM staff_roles
    WHERE user_id = auth.uid()
    AND role IN ('owner', 'admin')
    AND is_active = TRUE
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

-- Check if the calling user is owner
CREATE OR REPLACE FUNCTION is_owner()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM staff_roles
    WHERE user_id = auth.uid()
    AND role = 'owner'
    AND is_active = TRUE
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

-- Check if user is participant in a conversation
CREATE OR REPLACE FUNCTION is_conversation_participant(conv_id UUID)
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM conversation_participants
    WHERE conversation_id = conv_id
    AND user_id = auth.uid()
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

-- Check if user can access content (staff OR order manager OR content manager)
CREATE OR REPLACE FUNCTION can_manage_content()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM staff_roles
    WHERE user_id = auth.uid()
    AND role IN ('owner', 'admin', 'content_manager')
    AND is_active = TRUE
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION can_manage_orders()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM staff_roles
    WHERE user_id = auth.uid()
    AND role IN ('owner', 'admin', 'order_manager')
    AND is_active = TRUE
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION can_manage_conversations()
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM staff_roles
    WHERE user_id = auth.uid()
    AND role IN ('owner', 'admin', 'order_manager', 'support_agent')
    AND is_active = TRUE
  );
$$ LANGUAGE SQL SECURITY DEFINER STABLE;

-- ============================================================
-- PROFILES POLICIES
-- ============================================================

CREATE POLICY "Users can read own profile"
  ON profiles FOR SELECT
  USING (auth.uid() = id);

CREATE POLICY "Staff can read all profiles"
  ON profiles FOR SELECT
  USING (is_staff());

CREATE POLICY "Users can update own profile"
  ON profiles FOR UPDATE
  USING (auth.uid() = id)
  WITH CHECK (auth.uid() = id);

-- ============================================================
-- STAFF_ROLES POLICIES
-- ============================================================

CREATE POLICY "Staff can read own role"
  ON staff_roles FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Admins can read all roles"
  ON staff_roles FOR SELECT
  USING (is_admin_or_owner());

CREATE POLICY "Only owner can manage roles"
  ON staff_roles FOR ALL
  USING (is_owner())
  WITH CHECK (is_owner());

-- ============================================================
-- CATEGORIES POLICIES
-- ============================================================

CREATE POLICY "Anyone can read visible categories"
  ON categories FOR SELECT
  USING (is_visible = TRUE OR is_staff());

CREATE POLICY "Content managers can write categories"
  ON categories FOR INSERT
  WITH CHECK (can_manage_content());

CREATE POLICY "Content managers can update categories"
  ON categories FOR UPDATE
  USING (can_manage_content());

CREATE POLICY "Admins can delete categories"
  ON categories FOR DELETE
  USING (is_admin_or_owner());

-- ============================================================
-- PRODUCTS POLICIES
-- ============================================================

CREATE POLICY "Anyone can read published products"
  ON products FOR SELECT
  USING (
    (is_published = TRUE AND is_available = TRUE)
    OR is_staff()
  );

CREATE POLICY "Content managers can insert products"
  ON products FOR INSERT
  WITH CHECK (can_manage_content());

CREATE POLICY "Content managers can update products"
  ON products FOR UPDATE
  USING (can_manage_content());

CREATE POLICY "Admins can delete products"
  ON products FOR DELETE
  USING (is_admin_or_owner());

-- Product images / attributes / variants — same read policy
CREATE POLICY "Anyone can read product images"
  ON product_images FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM products p
      WHERE p.id = product_id
      AND (p.is_published = TRUE OR is_staff())
    )
  );

CREATE POLICY "Content managers can manage product images"
  ON product_images FOR ALL
  USING (can_manage_content())
  WITH CHECK (can_manage_content());

CREATE POLICY "Anyone can read product attributes"
  ON product_attributes FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM products p
      WHERE p.id = product_id
      AND (p.is_published = TRUE OR is_staff())
    )
  );

CREATE POLICY "Content managers can manage attributes"
  ON product_attributes FOR ALL
  USING (can_manage_content())
  WITH CHECK (can_manage_content());

CREATE POLICY "Anyone can read product variants"
  ON product_variants FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM products p
      WHERE p.id = product_id
      AND (p.is_published = TRUE OR is_staff())
    )
  );

CREATE POLICY "Content managers can manage variants"
  ON product_variants FOR ALL
  USING (can_manage_content())
  WITH CHECK (can_manage_content());

CREATE POLICY "Anyone can read published reviews"
  ON product_reviews FOR SELECT
  USING (is_published = TRUE OR user_id = auth.uid() OR is_staff());

CREATE POLICY "Authenticated users can submit reviews"
  ON product_reviews FOR INSERT
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own reviews"
  ON product_reviews FOR UPDATE
  USING (auth.uid() = user_id);

CREATE POLICY "Admins can manage reviews"
  ON product_reviews FOR ALL
  USING (is_admin_or_owner());

CREATE POLICY "Anyone can read related products"
  ON related_products FOR SELECT
  USING (TRUE);

-- ============================================================
-- FAVORITES POLICIES
-- ============================================================

CREATE POLICY "Users can read own favorites"
  ON favorites FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own favorites"
  ON favorites FOR INSERT
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own favorites"
  ON favorites FOR DELETE
  USING (auth.uid() = user_id);

-- ============================================================
-- ADDRESSES POLICIES
-- ============================================================

CREATE POLICY "Users can manage own addresses"
  ON addresses FOR ALL
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Staff can read addresses"
  ON addresses FOR SELECT
  USING (can_manage_orders());

-- ============================================================
-- CARTS POLICIES
-- ============================================================

CREATE POLICY "Users can manage own cart"
  ON carts FOR ALL
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can manage own cart items"
  ON cart_items FOR ALL
  USING (
    EXISTS (SELECT 1 FROM carts WHERE id = cart_id AND user_id = auth.uid())
  )
  WITH CHECK (
    EXISTS (SELECT 1 FROM carts WHERE id = cart_id AND user_id = auth.uid())
  );

-- ============================================================
-- ORDERS POLICIES
-- ============================================================

CREATE POLICY "Users can read own orders"
  ON orders FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Staff can read all orders"
  ON orders FOR SELECT
  USING (can_manage_orders());

-- Orders are created via RPC only (server-side), not direct insert
CREATE POLICY "Order managers can update orders"
  ON orders FOR UPDATE
  USING (can_manage_orders());

CREATE POLICY "Users can read own order items"
  ON order_items FOR SELECT
  USING (
    EXISTS (SELECT 1 FROM orders WHERE id = order_id AND user_id = auth.uid())
  );

CREATE POLICY "Staff can read all order items"
  ON order_items FOR SELECT
  USING (can_manage_orders());

CREATE POLICY "Users can read own order history"
  ON order_status_history FOR SELECT
  USING (
    EXISTS (SELECT 1 FROM orders WHERE id = order_id AND user_id = auth.uid())
  );

CREATE POLICY "Staff can read all order history"
  ON order_status_history FOR SELECT
  USING (can_manage_orders());

-- ============================================================
-- CONVERSATIONS POLICIES
-- ============================================================

CREATE POLICY "Users can read own conversations"
  ON conversations FOR SELECT
  USING (
    is_conversation_participant(id)
    OR can_manage_conversations()
  );

CREATE POLICY "Staff can read all conversations"
  ON conversations FOR SELECT
  USING (can_manage_conversations());

CREATE POLICY "Participants can read conversation participants"
  ON conversation_participants FOR SELECT
  USING (
    conversation_id IN (
      SELECT conversation_id FROM conversation_participants
      WHERE user_id = auth.uid()
    )
    OR can_manage_conversations()
  );

-- ============================================================
-- MESSAGES POLICIES
-- ============================================================

CREATE POLICY "Participants can read non-internal messages"
  ON messages FOR SELECT
  USING (
    (
      is_conversation_participant(conversation_id)
      AND (is_internal_note = FALSE OR sender_id = auth.uid())
    )
    OR can_manage_conversations()
  );

CREATE POLICY "Staff can read internal notes"
  ON messages FOR SELECT
  USING (is_internal_note = TRUE AND can_manage_conversations());

CREATE POLICY "Participants can send messages"
  ON messages FOR INSERT
  WITH CHECK (
    auth.uid() = sender_id
    AND is_conversation_participant(conversation_id)
    AND (is_internal_note = FALSE OR can_manage_conversations())
  );

CREATE POLICY "Staff can send internal notes"
  ON messages FOR INSERT
  WITH CHECK (
    auth.uid() = sender_id
    AND can_manage_conversations()
  );

CREATE POLICY "Staff can update message read status"
  ON messages FOR UPDATE
  USING (can_manage_conversations() OR auth.uid() = sender_id);

-- ============================================================
-- NOTIFICATIONS POLICIES
-- ============================================================

CREATE POLICY "Users can read own notifications"
  ON notifications FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Users can mark own notifications read"
  ON notifications FOR UPDATE
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- ============================================================
-- PUBLIC CONTENT POLICIES (Banners, Home Sections, Settings)
-- ============================================================

CREATE POLICY "Anyone can read active banners"
  ON banners FOR SELECT
  USING (is_active = TRUE OR can_manage_content());

CREATE POLICY "Content managers can manage banners"
  ON banners FOR ALL
  USING (can_manage_content())
  WITH CHECK (can_manage_content());

CREATE POLICY "Anyone can read enabled home sections"
  ON home_sections FOR SELECT
  USING (is_enabled = TRUE OR can_manage_content());

CREATE POLICY "Content managers can manage home sections"
  ON home_sections FOR ALL
  USING (can_manage_content())
  WITH CHECK (can_manage_content());

CREATE POLICY "Anyone can read home section items"
  ON home_section_items FOR SELECT
  USING (TRUE);

CREATE POLICY "Content managers can manage home section items"
  ON home_section_items FOR ALL
  USING (can_manage_content())
  WITH CHECK (can_manage_content());

CREATE POLICY "Anyone can read store settings"
  ON store_settings FOR SELECT
  USING (TRUE);

CREATE POLICY "Admins can manage store settings"
  ON store_settings FOR ALL
  USING (is_admin_or_owner())
  WITH CHECK (is_admin_or_owner());

CREATE POLICY "Anyone can read published pages"
  ON store_pages FOR SELECT
  USING (is_published = TRUE OR can_manage_content());

CREATE POLICY "Content managers can manage pages"
  ON store_pages FOR ALL
  USING (can_manage_content())
  WITH CHECK (can_manage_content());

CREATE POLICY "Staff can read saved replies"
  ON saved_replies FOR SELECT
  USING (can_manage_conversations());

CREATE POLICY "Admins can manage saved replies"
  ON saved_replies FOR ALL
  USING (is_admin_or_owner())
  WITH CHECK (is_admin_or_owner());

-- ============================================================
-- AUDIT LOG POLICIES
-- ============================================================

CREATE POLICY "Owners can read audit logs"
  ON audit_logs FOR SELECT
  USING (is_admin_or_owner());

-- Audit logs are inserted via SECURITY DEFINER functions only
-- No direct insert policy for client
