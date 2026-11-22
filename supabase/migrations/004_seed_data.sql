-- ============================================================
-- MYSHOP — Migration 004: Seed / Demo Data
-- NOTE: This is DEMO data only. Remove before production.
-- ============================================================

-- ============================================================
-- STORE SETTINGS (defaults)
-- ============================================================

INSERT INTO store_settings (namespace, key, value) VALUES
  -- Brand
  ('brand', 'store_name', 'MyShop'),
  ('brand', 'store_name_legal', 'MyShop LLC'),
  ('brand', 'tagline_ka', 'შენი ონლაინ მაღაზია'),
  ('brand', 'color_primary', '#5B21B6'),
  ('brand', 'color_accent', '#F59E0B'),
  -- Store Info
  ('store_info', 'phone_primary', '+995 555 123 456'),
  ('store_info', 'email_support', 'support@myshop.ge'),
  ('store_info', 'city', 'თბილისი'),
  ('store_info', 'country', 'საქართველო'),
  ('store_info', 'working_hours_ka', 'ორშაბათი–შაბათი: 10:00–20:00'),
  -- Social
  ('social', 'facebook', 'https://facebook.com/myshop.ge'),
  ('social', 'instagram', 'https://instagram.com/myshop.ge'),
  -- Catalog
  ('catalog', 'currency', 'GEL'),
  ('catalog', 'currency_symbol', '₾'),
  ('catalog', 'page_size', '20'),
  ('catalog', 'show_ratings', 'true'),
  ('catalog', 'show_stock', 'true'),
  -- Order
  ('order', 'number_prefix', 'ORD'),
  ('order', 'guest_checkout', 'false'),
  ('order', 'require_phone', 'true'),
  ('order', 'require_address', 'false'),
  ('order', 'confirmation_text_ka', 'თქვენი შეკვეთა მიღებულია! ადმინისტრატორი მალე დაგიკავშირდებათ.'),
  -- Delivery
  ('delivery', 'enabled', 'true'),
  ('delivery', 'pickup_enabled', 'true'),
  ('delivery', 'estimated_days_ka', '1-3 დღეში'),
  ('delivery', 'free_threshold', '50'),
  -- Chat
  ('chat', 'welcome_message_ka', 'გამარჯობა! MyShop-ის გუნდი მზადაა თქვენთვის. 🛍️'),
  ('chat', 'response_time_ka', 'ჩვეულებრივ ვპასუხობთ 1 საათის განმავლობაში'),
  -- Maintenance
  ('system', 'maintenance_mode', 'false'),
  ('system', 'min_app_version', '1.0.0')
ON CONFLICT (namespace, key) DO NOTHING;

-- ============================================================
-- STORE PAGES
-- ============================================================

INSERT INTO store_pages (slug, title_ka, content_ka, is_published) VALUES
  ('terms', 'წესები და პირობები', '# წესები და პირობები\n\nMyShop-ის გამოყენებით თქვენ ეთანხმებით ამ პირობებს...', TRUE),
  ('privacy', 'კონფიდენციალურობის პოლიტიკა', '# კონფიდენციალურობა\n\nჩვენ ვიცავთ თქვენს პირად ინფორმაციას...', TRUE),
  ('delivery', 'მიტანის პოლიტიკა', '# მიტანა\n\nთბილისში მიტანა: 1-2 სამუშაო დღე\nრეგიონებში: 2-5 სამუშაო დღე...', TRUE),
  ('returns', 'დაბრუნების პოლიტიკა', '# დაბრუნება\n\nპროდუქტის დაბრუნება შეგიძლიათ 14 დღის განმავლობაში...', TRUE),
  ('about', 'ჩვენ შესახებ', '# MyShop-ის შესახებ\n\nMyShop — საქართველოს ერთ-ერთი წამყვანი ონლაინ მაღაზია...', TRUE)
ON CONFLICT (slug) DO NOTHING;

-- ============================================================
-- CATEGORIES (Demo)
-- ============================================================

INSERT INTO categories (id, name_ka, slug, sort_order, is_visible) VALUES
  ('c1000000-0000-0000-0000-000000000001', 'ელექტრონიკა', 'electronics', 1, TRUE),
  ('c1000000-0000-0000-0000-000000000002', 'ტანსაცმელი', 'clothing', 2, TRUE),
  ('c1000000-0000-0000-0000-000000000003', 'სასლური ტექნიკა', 'home-appliances', 3, TRUE),
  ('c1000000-0000-0000-0000-000000000004', 'სილამაზე და კავთმელება', 'beauty', 4, TRUE),
  ('c1000000-0000-0000-0000-000000000005', 'სპორტი და გართობა', 'sports', 5, TRUE),
  ('c1000000-0000-0000-0000-000000000006', 'სათამაშოები', 'toys', 6, TRUE),
  ('c1000000-0000-0000-0000-000000000007', 'აქსესუარები', 'accessories', 7, TRUE),
  ('c1000000-0000-0000-0000-000000000008', 'სასლი და სამზარეულო', 'home-kitchen', 8, TRUE)
ON CONFLICT (slug) DO NOTHING;

-- ============================================================
-- PRODUCTS (Demo)
-- ============================================================

INSERT INTO products (
  id, category_id, name_ka, slug, short_description_ka,
  price, previous_price, discount_percent,
  stock_quantity, is_published, is_available, is_featured, is_popular, is_new,
  average_rating, review_count, main_image_url
) VALUES
  (
    'p1000000-0000-0000-0000-000000000001',
    'c1000000-0000-0000-0000-000000000001',
    'iPhone 17', 'iphone-17',
    '256GB, Black',
    2299.00, 2699.00, 15,
    45, TRUE, TRUE, TRUE, TRUE, FALSE,
    4.8, 1240,
    'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400'
  ),
  (
    'p1000000-0000-0000-0000-000000000002',
    'c1000000-0000-0000-0000-000000000001',
    'AirPods Pro 3', 'airpods-pro-3',
    'White',
    899.00, 1099.00, 20,
    82, TRUE, TRUE, FALSE, TRUE, FALSE,
    4.8, 860,
    'https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=400'
  ),
  (
    'p1000000-0000-0000-0000-000000000003',
    'c1000000-0000-0000-0000-000000000001',
    'Smart Watch T900', 'smart-watch-t900',
    'Black',
    89.00, 119.00, 25,
    120, TRUE, TRUE, FALSE, TRUE, FALSE,
    4.7, 720,
    'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400'
  ),
  (
    'p1000000-0000-0000-0000-000000000004',
    'c1000000-0000-0000-0000-000000000001',
    'უსადენო ყურასმენები Bluetooth 5.3', 'wireless-earbuds-bt53',
    'Bluetooth 5.3',
    69.00, 85.00, 18,
    200, TRUE, TRUE, FALSE, FALSE, TRUE,
    4.7, 540,
    'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400'
  ),
  (
    'p1000000-0000-0000-0000-000000000005',
    'c1000000-0000-0000-0000-000000000001',
    'Lenovo IdeaPad 3', 'lenovo-ideapad-3',
    '15.6" FHD, 8GB, 512GB',
    1399.00, 1999.00, 30,
    15, TRUE, TRUE, FALSE, FALSE, FALSE,
    4.8, 430,
    'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400'
  ),
  (
    'p1000000-0000-0000-0000-000000000006',
    'c1000000-0000-0000-0000-000000000001',
    'JBL Flip 6', 'jbl-flip-6',
    'Bluetooth Speaker',
    249.00, 319.00, 22,
    60, TRUE, TRUE, FALSE, TRUE, FALSE,
    4.7, 380,
    'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400'
  )
ON CONFLICT (slug) DO NOTHING;

-- ============================================================
-- HOME SECTIONS (Default layout)
-- ============================================================

INSERT INTO home_sections (id, type, title_ka, sort_order, is_enabled) VALUES
  ('hs000000-0000-0000-0000-000000000001', 'hero_banners', NULL, 1, TRUE),
  ('hs000000-0000-0000-0000-000000000002', 'category_shortcuts', 'კატეგორიები', 2, TRUE),
  ('hs000000-0000-0000-0000-000000000003', 'promo_cards', NULL, 3, TRUE),
  ('hs000000-0000-0000-0000-000000000004', 'popular_products', 'პოპულარული პროდუქტები', 4, TRUE),
  ('hs000000-0000-0000-0000-000000000005', 'discounted_products', 'ფასდაკლებული პროდუქტები', 5, TRUE),
  ('hs000000-0000-0000-0000-000000000006', 'new_products', 'ახალი პროდუქტები', 6, TRUE)
ON CONFLICT DO NOTHING;

-- ============================================================
-- BANNERS (Demo hero)
-- ============================================================

INSERT INTO banners (id, type, title_ka, subtitle_ka, cta_label_ka, sort_order, is_active) VALUES
  (
    'b1000000-0000-0000-0000-000000000001',
    'hero',
    'უკეთესი შეთავაზებები MyShop-ში!',
    'ახლავე შეიძინე საუკეთესო პროდუქტები',
    'იხილე შეთავაზებები',
    1, TRUE
  )
ON CONFLICT DO NOTHING;

-- Link banners to home section
INSERT INTO home_section_items (section_id, banner_id, sort_order) VALUES
  ('hs000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 1)
ON CONFLICT DO NOTHING;

-- Link categories to shortcuts section
INSERT INTO home_section_items (section_id, category_id, sort_order)
SELECT 'hs000000-0000-0000-0000-000000000002', id, sort_order
FROM categories WHERE is_visible = TRUE ORDER BY sort_order LIMIT 5
ON CONFLICT DO NOTHING;

-- Link popular products to section
INSERT INTO home_section_items (section_id, product_id, sort_order)
SELECT 'hs000000-0000-0000-0000-000000000004', id, ROW_NUMBER() OVER (ORDER BY review_count DESC)
FROM products WHERE is_popular = TRUE AND is_published = TRUE
ON CONFLICT DO NOTHING;

-- ============================================================
-- SAVED REPLIES (Demo templates)
-- ============================================================

INSERT INTO saved_replies (title_ka, content_ka) VALUES
  ('გამარჯობა', 'გამარჯობა! MyShop-ში მოგესალმებით. როგორ შეგვიძლია დაგეხმაროთ?'),
  ('გადახდის პირობები', 'გადახდა შეგიძლიათ მიტანისას ნაღდი ფულით ან საბანკო გადარიცხვით.'),
  ('მიტანის ვადა', 'თბილისში მიტანა: 1-2 სამუშაო დღე. რეგიონებში: 3-5 სამუშაო დღე.'),
  ('შეკვეთა დამტკიცდა', 'თქვენი შეკვეთა დამტკიცდა! მალე მომზადდება გასაგზავნად.'),
  ('გმადლობთ', 'გმადლობთ MyShop-ში შეძენისთვის! ველოდებით თქვენს მომდევნო ვიზიტს. 🛍️')
ON CONFLICT DO NOTHING;
