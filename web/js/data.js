// ── Product data ──────────────────────────────────────────────
export const CATEGORIES = [
  { id: 'smartphones', emoji: '📱', name: 'სმართფონები' },
  { id: 'computers',   emoji: '💻', name: 'კომპიუტერები' },
  { id: 'audio',       emoji: '🎧', name: 'აუდიო' },
  { id: 'watches',     emoji: '⌚', name: 'სმარტ საათები' },
  { id: 'clothing',    emoji: '👕', name: 'ტანსაცმელი' },
];

export const PRODUCTS = [
  {
    id: 1, cat: 'smartphones',
    name: 'Samsung Galaxy S25 Ultra', variant: '256GB / Titanium Black',
    emoji: '📱', price: 3299, oldPrice: 3799, discount: 13,
    rating: 4.8, reviews: 1247,
    tags: ['5G', 'AI კამერა', 'S Pen', '200MP'],
    desc: 'Galaxy AI-ით გაძლიერებული ფლაგმანი. 200MP კამერა, S Pen, 5000mAh ბატარეა.',
    inStock: true,
  },
  {
    id: 2, cat: 'smartphones',
    name: 'iPhone 16 Pro Max', variant: '512GB / Desert Titanium',
    emoji: '📱', price: 4199, oldPrice: 4499, discount: 7,
    rating: 4.9, reviews: 2143,
    tags: ['A18 Pro', 'ProMotion', 'USB-C', 'Action Button'],
    desc: 'Apple-ის ყველაზე ძლიერი iPhone. A18 Pro ჩიპი, 48MP კამერა სისტემა.',
    inStock: true,
  },
  {
    id: 3, cat: 'computers',
    name: 'MacBook Pro 16"', variant: 'M4 Pro / 24GB / 512GB',
    emoji: '💻', price: 5699, oldPrice: 6199, discount: 8,
    rating: 4.9, reviews: 876,
    tags: ['M4 Pro', 'Liquid Retina', '22hr Battery', 'MagSafe'],
    desc: 'პროფესიონალთა არჩევანი. M4 Pro ჩიპი, 24GB Unified Memory, 22 საათი მუშაობა.',
    inStock: true,
  },
  {
    id: 4, cat: 'computers',
    name: 'Dell XPS 15', variant: 'Core Ultra 9 / 32GB / 1TB',
    emoji: '💻', price: 4299, oldPrice: 4799, discount: 10,
    rating: 4.7, reviews: 543,
    tags: ['OLED', 'RTX 4070', 'Core Ultra 9', 'Thunderbolt 4'],
    desc: 'Windows-ის პრემიუმ ლეპტოპი. OLED ეკრანი, RTX 4070 გრაფიკა.',
    inStock: true,
  },
  {
    id: 5, cat: 'audio',
    name: 'Sony WH-1000XM6', variant: 'Midnight Black',
    emoji: '🎧', price: 999, oldPrice: 1199, discount: 17,
    rating: 4.8, reviews: 3421,
    tags: ['ANC', 'LDAC', '30hr Battery', 'Multipoint'],
    desc: 'ინდუსტრიის საუკეთესო ხმის ჩახშობა. LDAC Hi-Res Audio, 30 საათი ბატარეა.',
    inStock: true,
  },
  {
    id: 6, cat: 'audio',
    name: 'AirPods Pro 3', variant: 'USB-C / MagSafe',
    emoji: '🎧', price: 799, oldPrice: 899, discount: 11,
    rating: 4.7, reviews: 2876,
    tags: ['H2 Chip', 'ANC', 'Transparency', 'Spatial Audio'],
    desc: 'Apple-ის საუკეთესო True Wireless. H2 ჩიპი, განახლებული ANC, Spatial Audio.',
    inStock: true,
  },
  {
    id: 7, cat: 'watches',
    name: 'Apple Watch Ultra 3', variant: '49mm / Black Titanium',
    emoji: '⌚', price: 2499, oldPrice: 2799, discount: 11,
    rating: 4.9, reviews: 1123,
    tags: ['Titanium', 'L1+L5 GPS', '72hr Battery', 'microLED'],
    desc: 'ექსტრემალური გარემოსთვის შექმნილი სმარტ საათი. Titanium კორპუსი, 72 საათი ბატარეა.',
    inStock: true,
  },
  {
    id: 8, cat: 'watches',
    name: 'Samsung Galaxy Watch 8 Pro', variant: '47mm / Titanium Gray',
    emoji: '⌚', price: 1399, oldPrice: 1599, discount: 13,
    rating: 4.6, reviews: 789,
    tags: ['Titanium', 'Health Sensors', '4G LTE', 'Sapphire Glass'],
    desc: 'Galaxy AI ჯანმრთელობის მენეჯერი. ტიტანის კორპუსი, სამედიცინო სენსორები.',
    inStock: true,
  },
  {
    id: 9, cat: 'clothing',
    name: 'Nike Air Max 2025', variant: 'Triple White / US 42',
    emoji: '👟', price: 449, oldPrice: 549, discount: 18,
    rating: 4.5, reviews: 2341,
    tags: ['Air Max', 'React Foam', 'Flyknit', 'Premium'],
    desc: 'Nike-ის ახალი Air Max თაობა. React ქაფი, Flyknit ზედა, Air Max ამორტიზაცია.',
    inStock: true,
  },
  {
    id: 10, cat: 'clothing',
    name: 'Adidas Ultraboost 25', variant: 'Core Black / US 42',
    emoji: '👟', price: 399, oldPrice: 499, discount: 20,
    rating: 4.6, reviews: 1876,
    tags: ['Boost', 'Primeknit+', 'Continental', 'Linear Energy'],
    desc: 'სრულყოფილი სარბენი ფეხსაცმელი. Boost ამორტიზაცია, Primeknit+ ზედა.',
    inStock: true,
  },
];

export const OFFERS = [
  { id: 1, title: 'Flash Sale! 50% Off', sub: 'სმართფონებზე დღეს', emoji: '⚡', bg: 'linear-gradient(135deg,#FF416C,#FF4B2B)', ends: Date.now() + 3*3600*1000 },
  { id: 2, title: '2+1 უფასო', sub: 'ყველა ტანსაცმელზე', emoji: '🎁', bg: 'linear-gradient(135deg,#4776E6,#8E54E9)', ends: Date.now() + 24*3600*1000 },
  { id: 3, title: 'უფასო მიწოდება', sub: '299₾-ზე მეტ შეკვეთაზე', emoji: '🚚', bg: 'linear-gradient(135deg,#11998e,#38ef7d)', ends: null },
];

export const SLIDES = [
  {
    emoji: '🛍️',
    title: 'ახალი კოლექცია\n2025',
    sub: 'საუკეთესო ბრენდები ერთ ადგილას',
    cta: 'ახლავე იყიდე',
    bg: 'linear-gradient(135deg, #2d0a8f 0%, #5B21E6 50%, #7C4AF0 100%)',
  },
  {
    emoji: '📱',
    title: 'iPhone 16 Pro\nახლა ხელმისაწვდომია',
    sub: 'Apple-ის ახალი ფლაგმანი',
    cta: 'ნახე მეტი',
    bg: 'linear-gradient(135deg, #1a0050 0%, #4a17c8 50%, #7C4AF0 100%)',
  },
  {
    emoji: '💰',
    title: 'Black Friday\nზე ადრე!',
    sub: '50%-მდე ფასდაკლება ყველა კატეგორიაზე',
    cta: 'ფასდაკლებები',
    bg: 'linear-gradient(135deg, #0d0030 0%, #3d0eb3 50%, #5B21E6 100%)',
  },
];

export const QUICK_REPLIES = [
  'გამარჯობა! 👋',
  'ფასის შეკითხვა',
  'მიწოდება როდის?',
  'გარანტია?',
  'დაბრუნების პოლიტიკა',
];

export const BOT_RESPONSES = {
  default: [
    'გამარჯობა! MyShop-ის ასისტენტი ვარ 🤖 როგორ შეგიძლია დაგეხმაროთ?',
    'რა პროდუქტი გაინტერესებს? შეგიძლია კონკრეტული კატეგორია მომითხოვო.',
    'კარგი კითხვაა! ჩვენ 24/7 ვმუშაობთ. სხვა კითხვა გაქვს?',
    'გმადლობ კონტაქტისთვის! მოკლე ხანში ოპერატორი გიპასუხებს.',
  ],
  greeting: 'გამარჯობა! 😊 MyShop-ში გხვდები. რა შეგვიძლია შენთვის გავაკეთოთ?',
  delivery: '🚚 მიწოდება: თბილისი - 1-2 დღე (ქ.5₾), რეგიონი - 2-5 დღე (10₾). 299₾-ზე მეტ შეკვეთაზე - უფასო!',
  warranty: '🛡️ ყველა პროდუქტს აქვს მწარმოებლის გარანტია (12-24 თვე). ჩვენ ასევე გთავაზობთ 30 დღიანი დაბრუნების გარანტიას.',
  return: '↩️ 30 დღის განმავლობაში შეგიძლია დააბრუნო ნებისმიერი პროდუქტი. უბრალოდ დაგვიკავშირდი და ჩვენ ორგანიზებას გავაკეთებთ!',
  price: '💰 ფასები ყოველდღიურად განახლდება. Flash Sale-ები და სპეციალური შეთავაზებები ხშირად ქვეყნდება!',
};
