package com.example.i18n

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ARABIC("ar", "العربية", true),
    FRENCH("fr", "Français", false),
    ENGLISH("en", "English", false)
}

object AppStrings {
    // Current active language
    var currentLanguage = AppLanguage.ARABIC

    fun get(key: String, lang: AppLanguage = currentLanguage): String {
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.ARABIC) ?: key
    }

    private val translations = mapOf(
        "app_title" to mapOf(
            AppLanguage.ARABIC to "القصر الذهبي",
            AppLanguage.FRENCH to "Le Palais Doré",
            AppLanguage.ENGLISH to "Golden Palace"
        ),
        "app_subtitle" to mapOf(
            AppLanguage.ARABIC to "عند الجيجلي • حسين داي",
            AppLanguage.FRENCH to "Chez Djidjeli • Hussein Dey",
            AppLanguage.ENGLISH to "Chez Djidjeli • Hussein Dey"
        ),
        "restaurant_address" to mapOf(
            AppLanguage.ARABIC to "شارع بلهوشات، حسين داي، الجزائر - بجانب فندق Oasis",
            AppLanguage.FRENCH to "Rue Belhouchet, Hussein Dey, Alger - à côté de l'Hôtel Oasis",
            AppLanguage.ENGLISH to "Belhouchet Street, Hussein Dey, Algiers - next to Oasis Hotel"
        ),
        "restaurant_phone" to mapOf(
            AppLanguage.ARABIC to "0791755614",
            AppLanguage.FRENCH to "0791755614",
            AppLanguage.ENGLISH to "0791755614"
        ),
        "currency" to mapOf(
            AppLanguage.ARABIC to "دج",
            AppLanguage.FRENCH to "DA",
            AppLanguage.ENGLISH to "DZD"
        ),
        "login_title" to mapOf(
            AppLanguage.ARABIC to "تسجيل الدخول إلى النظام",
            AppLanguage.FRENCH to "Connexion au Système",
            AppLanguage.ENGLISH to "Sign In to POS System"
        ),
        "username" to mapOf(
            AppLanguage.ARABIC to "اسم المستخدم",
            AppLanguage.FRENCH to "Nom d'utilisateur",
            AppLanguage.ENGLISH to "Username"
        ),
        "password" to mapOf(
            AppLanguage.ARABIC to "كلمة المرور",
            AppLanguage.FRENCH to "Mot de passe",
            AppLanguage.ENGLISH to "Password"
        ),
        "login_button" to mapOf(
            AppLanguage.ARABIC to "دخول",
            AppLanguage.FRENCH to "Connexion",
            AppLanguage.ENGLISH to "Login"
        ),
        "pos" to mapOf(
            AppLanguage.ARABIC to "نقطة البيع (الكاشير)",
            AppLanguage.FRENCH to "Point de Vente (Caisse)",
            AppLanguage.ENGLISH to "POS Cashier"
        ),
        "tables" to mapOf(
            AppLanguage.ARABIC to "إدارة الطاولات",
            AppLanguage.FRENCH to "Gestion des Tables",
            AppLanguage.ENGLISH to "Table Management"
        ),
        "kitchen" to mapOf(
            AppLanguage.ARABIC to "شاشة المطبخ (KDS)",
            AppLanguage.FRENCH to "Écran Cuisine (KDS)",
            AppLanguage.ENGLISH to "Kitchen Display (KDS)"
        ),
        "orders" to mapOf(
            AppLanguage.ARABIC to "سجل الطلبات",
            AppLanguage.FRENCH to "Historique des Commandes",
            AppLanguage.ENGLISH to "Orders Log"
        ),
        "inventory" to mapOf(
            AppLanguage.ARABIC to "المخزون والوصفات",
            AppLanguage.FRENCH to "Stock et Recettes",
            AppLanguage.ENGLISH to "Inventory & Recipes"
        ),
        "reports" to mapOf(
            AppLanguage.ARABIC to "التقارير والأرباح",
            AppLanguage.FRENCH to "Rapports & Bénéfices",
            AppLanguage.ENGLISH to "Reports & Profits"
        ),
        "cash_register" to mapOf(
            AppLanguage.ARABIC to "إدارة الصندوق والورديات",
            AppLanguage.FRENCH to "Caisse & Quarts",
            AppLanguage.ENGLISH to "Cash Register & Shifts"
        ),
        "expenses" to mapOf(
            AppLanguage.ARABIC to "المصاريف اليومية",
            AppLanguage.FRENCH to "Dépenses Quotidiennes",
            AppLanguage.ENGLISH to "Daily Expenses"
        ),
        "settings" to mapOf(
            AppLanguage.ARABIC to "الإعدادات والنسخ الاحتياطي",
            AppLanguage.FRENCH to "Paramètres & Sauvegarde",
            AppLanguage.ENGLISH to "Settings & Backup"
        ),
        "logout" to mapOf(
            AppLanguage.ARABIC to "تسجيل الخروج",
            AppLanguage.FRENCH to "Déconnexion",
            AppLanguage.ENGLISH to "Logout"
        ),
        "dine_in" to mapOf(
            AppLanguage.ARABIC to "محلي (داخل المطعم)",
            AppLanguage.FRENCH to "Sur place",
            AppLanguage.ENGLISH to "Dine In"
        ),
        "takeaway" to mapOf(
            AppLanguage.ARABIC to "سفري (Takeaway)",
            AppLanguage.FRENCH to "À emporter",
            AppLanguage.ENGLISH to "Takeaway"
        ),
        "delivery" to mapOf(
            AppLanguage.ARABIC to "توصيل (Delivery)",
            AppLanguage.FRENCH to "Livraison",
            AppLanguage.ENGLISH to "Delivery"
        ),
        "current_order" to mapOf(
            AppLanguage.ARABIC to "الطلب الحالي",
            AppLanguage.FRENCH to "Commande en cours",
            AppLanguage.ENGLISH to "Current Order"
        ),
        "subtotal" to mapOf(
            AppLanguage.ARABIC to "المجموع الفرعي",
            AppLanguage.FRENCH to "Sous-total",
            AppLanguage.ENGLISH to "Subtotal"
        ),
        "discount" to mapOf(
            AppLanguage.ARABIC to "الخصم",
            AppLanguage.FRENCH to "Remise",
            AppLanguage.ENGLISH to "Discount"
        ),
        "total" to mapOf(
            AppLanguage.ARABIC to "الإجمالي",
            AppLanguage.FRENCH to "Total",
            AppLanguage.ENGLISH to "Total"
        ),
        "pay" to mapOf(
            AppLanguage.ARABIC to "الدفع",
            AppLanguage.FRENCH to "Payer",
            AppLanguage.ENGLISH to "Payment"
        ),
        "send_to_kitchen" to mapOf(
            AppLanguage.ARABIC to "إرسال للمطبخ",
            AppLanguage.FRENCH to "Envoyer en cuisine",
            AppLanguage.ENGLISH to "Send to Kitchen"
        ),
        "clear_cart" to mapOf(
            AppLanguage.ARABIC to "مسح السلة",
            AppLanguage.FRENCH to "Vider le panier",
            AppLanguage.ENGLISH to "Clear Cart"
        ),
        "empty_cart" to mapOf(
            AppLanguage.ARABIC to "السلة فارغة. اضغط على أي صنف لإضافته للطلب.",
            AppLanguage.FRENCH to "Panier vide. Cliquez sur un article pour l'ajouter.",
            AppLanguage.ENGLISH to "Cart is empty. Tap any product to add."
        ),
        "table" to mapOf(
            AppLanguage.ARABIC to "طاولة",
            AppLanguage.FRENCH to "Table",
            AppLanguage.ENGLISH to "Table"
        ),
        "all_categories" to mapOf(
            AppLanguage.ARABIC to "الكل",
            AppLanguage.FRENCH to "Tous",
            AppLanguage.ENGLISH to "All"
        ),
        "search_hint" to mapOf(
            AppLanguage.ARABIC to "بحث بالاسم أو مسح الباركود...",
            AppLanguage.FRENCH to "Rechercher ou scanner code-barres...",
            AppLanguage.ENGLISH to "Search by name or scan barcode..."
        ),
        "cash" to mapOf(
            AppLanguage.ARABIC to "نقداً (Cash)",
            AppLanguage.FRENCH to "Espèces",
            AppLanguage.ENGLISH to "Cash"
        ),
        "card" to mapOf(
            AppLanguage.ARABIC to "بطاقة بنكية",
            AppLanguage.FRENCH to "Carte Bancaire",
            AppLanguage.ENGLISH to "Card"
        ),
        "baridimob" to mapOf(
            AppLanguage.ARABIC to "بريدي موب (BaridiMob)",
            AppLanguage.FRENCH to "BaridiMob",
            AppLanguage.ENGLISH to "BaridiMob"
        ),
        "ccp" to mapOf(
            AppLanguage.ARABIC to "حساب بريدي (CCP)",
            AppLanguage.FRENCH to "Virement CCP",
            AppLanguage.ENGLISH to "CCP Transfer"
        ),
        "amount_received" to mapOf(
            AppLanguage.ARABIC to "المبلغ المستلم",
            AppLanguage.FRENCH to "Montant reçu",
            AppLanguage.ENGLISH to "Amount Received"
        ),
        "change" to mapOf(
            AppLanguage.ARABIC to "الباقي للعميل",
            AppLanguage.FRENCH to "Monnaie à rendre",
            AppLanguage.ENGLISH to "Change Due"
        ),
        "print_receipt" to mapOf(
            AppLanguage.ARABIC to "طباعة الفاتورة",
            AppLanguage.FRENCH to "Imprimer le reçu",
            AppLanguage.ENGLISH to "Print Receipt"
        ),
        "split_bill" to mapOf(
            AppLanguage.ARABIC to "تقسيم الفاتورة",
            AppLanguage.FRENCH to "Diviser la note",
            AppLanguage.ENGLISH to "Split Bill"
        ),
        "thank_you" to mapOf(
            AppLanguage.ARABIC to "شكراً لزيارتكم • صحة وهنا",
            AppLanguage.FRENCH to "Merci de votre visite • Bon appétit",
            AppLanguage.ENGLISH to "Thank you for visiting • Bon Appétit"
        ),
        "menu_management" to mapOf(
            AppLanguage.ARABIC to "إدارة قائمة الطعام",
            AppLanguage.FRENCH to "Gestion du Menu",
            AppLanguage.ENGLISH to "Menu Management"
        ),
        "add_product" to mapOf(
            AppLanguage.ARABIC to "+ إضافة منتج",
            AppLanguage.FRENCH to "+ Ajouter Produit",
            AppLanguage.ENGLISH to "+ Add Product"
        ),
        "edit_product" to mapOf(
            AppLanguage.ARABIC to "تعديل المنتج",
            AppLanguage.FRENCH to "Modifier le Produit",
            AppLanguage.ENGLISH to "Edit Product"
        ),
        "delete_product" to mapOf(
            AppLanguage.ARABIC to "حذف المنتج",
            AppLanguage.FRENCH to "Supprimer le Produit",
            AppLanguage.ENGLISH to "Delete Product"
        ),
        "product_name_ar" to mapOf(
            AppLanguage.ARABIC to "اسم المنتج بالعربية",
            AppLanguage.FRENCH to "Nom en Arabe",
            AppLanguage.ENGLISH to "Arabic Name"
        ),
        "product_name_fr" to mapOf(
            AppLanguage.ARABIC to "اسم المنتج بالفرنسية",
            AppLanguage.FRENCH to "Nom en Français",
            AppLanguage.ENGLISH to "French Name"
        ),
        "product_name_en" to mapOf(
            AppLanguage.ARABIC to "اسم المنتج بالإنجليزية",
            AppLanguage.FRENCH to "Nom en Anglais",
            AppLanguage.ENGLISH to "English Name"
        ),
        "category" to mapOf(
            AppLanguage.ARABIC to "التصنيف",
            AppLanguage.FRENCH to "Catégorie",
            AppLanguage.ENGLISH to "Category"
        ),
        "price" to mapOf(
            AppLanguage.ARABIC to "السعر (دج)",
            AppLanguage.FRENCH to "Prix (DA)",
            AppLanguage.ENGLISH to "Price (DZD)"
        ),
        "cost" to mapOf(
            AppLanguage.ARABIC to "التكلفة (دج)",
            AppLanguage.FRENCH to "Coût (DA)",
            AppLanguage.ENGLISH to "Cost (DZD)"
        ),
        "kitchen_station" to mapOf(
            AppLanguage.ARABIC to "قسم المطبخ المستلم",
            AppLanguage.FRENCH to "Poste de Cuisine",
            AppLanguage.ENGLISH to "Kitchen Station"
        ),
        "barcode_optional" to mapOf(
            AppLanguage.ARABIC to "الباركود (اختياري)",
            AppLanguage.FRENCH to "Code-barres (optionnel)",
            AppLanguage.ENGLISH to "Barcode (Optional)"
        ),
        "availability" to mapOf(
            AppLanguage.ARABIC to "حالة التوفر",
            AppLanguage.FRENCH to "Disponibilité",
            AppLanguage.ENGLISH to "Availability"
        ),
        "available" to mapOf(
            AppLanguage.ARABIC to "متوفر للبيع",
            AppLanguage.FRENCH to "Disponible",
            AppLanguage.ENGLISH to "Available"
        ),
        "unavailable" to mapOf(
            AppLanguage.ARABIC to "غير متوفر (مخفي من الكاشير)",
            AppLanguage.FRENCH to "Non disponible",
            AppLanguage.ENGLISH to "Unavailable"
        ),
        "description" to mapOf(
            AppLanguage.ARABIC to "الوصف",
            AppLanguage.FRENCH to "Description",
            AppLanguage.ENGLISH to "Description"
        ),
        "recipe_ingredients" to mapOf(
            AppLanguage.ARABIC to "مكونات الوجبة (الخصم من المخزون تلقائياً)",
            AppLanguage.FRENCH to "Ingrédients (Déduction automatique)",
            AppLanguage.ENGLISH to "Recipe & Inventory Deduction"
        ),
        "add_ingredient" to mapOf(
            AppLanguage.ARABIC to "+ إضافة مكوّن للوجبة",
            AppLanguage.FRENCH to "+ Ajouter ingrédient",
            AppLanguage.ENGLISH to "+ Add Ingredient"
        ),
        "quantity_needed" to mapOf(
            AppLanguage.ARABIC to "الكمية لكل وجبة",
            AppLanguage.FRENCH to "Quantité / portion",
            AppLanguage.ENGLISH to "Qty / portion"
        ),
        "all_products" to mapOf(
            AppLanguage.ARABIC to "كل الأصناف",
            AppLanguage.FRENCH to "Tous les produits",
            AppLanguage.ENGLISH to "All Products"
        )
    )
}
