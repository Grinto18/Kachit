package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PosDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        TableEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        InventoryItemEntity::class,
        RecipeItemEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        ExpenseEntity::class,
        CashSessionEntity::class,
        AuditLogEntity::class,
        CustomerEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "palais_dore_pos.db"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.posDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: PosDao) {
            // 1. Initial Users
            val users = listOf(
                UserEntity("u_admin", "admin", "admin123", "مدير النظام (Admin)", "ADMIN"),
                UserEntity("u_cashier", "cashier", "cashier123", "أحمد الجيجلي (كاشير)", "CASHIER"),
                UserEntity("u_manager", "manager", "manager123", "ياسين مسير (مدير الصالة)", "MANAGER"),
                UserEntity("u_waiter", "waiter", "waiter123", "كريم بلقاسم (نادل)", "WAITER"),
                UserEntity("u_kitchen", "kitchen", "kitchen123", "شيف مصطفى (رئيس المطبخ)", "KITCHEN")
            )
            users.forEach { dao.insertUser(it) }

            // 2. Categories
            val categories = listOf(
                CategoryEntity("cat_main", "أطباق تقليدية", "Plats Traditionnels", "Traditional Dishes", "dinner_dining", 1),
                CategoryEntity("cat_grill", "مشاوي فاخرة", "Grillades", "Grill & BBQ", "outdoor_grill", 2),
                CategoryEntity("cat_chicken", "دجاج ولحوم", "Poulet & Viandes", "Chicken & Meat", "kebab_dining", 3),
                CategoryEntity("cat_rice_sides", "أرز ومقبلات", "Riz & Entrées", "Rice & Sides", "rice_bowl", 4),
                CategoryEntity("cat_salads", "سلطات وشوربات", "Salades & Soupes", "Salads & Soups", "soup_kitchen", 5),
                CategoryEntity("cat_drinks", "مشروبات منعشة", "Boissons", "Beverages", "local_bar", 6),
                CategoryEntity("cat_dessert", "حلويات وتحليات", "Desserts", "Desserts", "bakery_dining", 7)
            )
            dao.insertCategories(categories)

            // 3. Products
            val products = listOf(
                ProductEntity("p_1", "شخشوخة بسكرية بالدجاج", "Chakhchoukha Poulet", "Chicken Chakhchoukha", "cat_main", 1200.0, 500.0, "613000001", "MAIN_KITCHEN", true, "شخشوخة حارة مرق أحمر مع دجاج وحمص"),
                ProductEntity("p_2", "تريدة قسنطينية باللحم", "Trida Constantinoise Viande", "Constantine Trida Lamb", "cat_main", 1400.0, 650.0, "613000002", "MAIN_KITCHEN", true, "تريدة رقيقة بالمرق الأبيض واللحم والخضار"),
                ProductEntity("p_3", "نصف دجاج مشوي مع الأرز", "1/2 Poulet Grillé + Riz", "1/2 Charcoal Grilled Chicken", "cat_grill", 850.0, 380.0, "613000003", "GRILL", true, "نصف دجاجة على الجمر مع صحن أرز وسلطة"),
                ProductEntity("p_4", "دجاج مشوي كامل على الفحم", "Poulet Grillé Entier", "Whole Grilled Chicken", "cat_grill", 1500.0, 700.0, "613000004", "GRILL", true, "دجاجة كاملة متبلة ومطهوة على الفحم"),
                ProductEntity("p_5", "مشوي مشكل القصر الذهبي", "Mix Grill Palais Doré", "Palais Doré Mix Grill", "cat_grill", 3500.0, 1600.0, "613000005", "GRILL", true, "كفتة، شواء لحم، كوتلات غنم، ودجاج مشوي"),
                ProductEntity("p_6", "كفتة مشوية على الجمر", "Kefta Braisée", "Charcoal Grilled Kefta", "cat_grill", 1100.0, 500.0, "613000006", "GRILL", true, "أسياخ كفتة لحم عجل مفروم مع التوابل"),
                ProductEntity("p_7", "لحم محمر بالفرن مع البطاطا", "Viande Rôtie au Four", "Oven Roasted Lamb", "cat_chicken", 1800.0, 850.0, "613000007", "MAIN_KITCHEN", true, "لحم خروف طري محمر مع بطاطا وسلطة"),
                ProductEntity("p_8", "صحن أرز بسمتي بالزعفران", "Riz Basmati Safran", "Saffron Basmati Rice", "cat_rice_sides", 450.0, 150.0, "613000008", "MAIN_KITCHEN", true, "أرز بسمتي فاخر مطهو بالزعفران"),
                ProductEntity("p_9", "شربة فريك بلحم الغنم", "Chorba Frik Traditionnelle", "Algerian Lamb Frik Soup", "cat_salads", 400.0, 180.0, "613000009", "MAIN_KITCHEN", true, "شربة الفريك العاصمية الكلاسيكية"),
                ProductEntity("p_10", "حميس حار بزيت الزيتون", "Hmiss Traditionnel", "Spicy Hmiss Salad", "cat_salads", 300.0, 100.0, "613000010", "MAIN_KITCHEN", true, "فلفل وطماطم مشويين ومتبلين بزيت الزيتون"),
                ProductEntity("p_11", "سلطة مشوية جزائرية", "Salade Mechouia", "Mechouia Salad", "cat_salads", 350.0, 120.0, "613000011", "MAIN_KITCHEN", true, "سلطة مشوية بالبيض المسلوق والزيتون"),
                ProductEntity("p_12", "بطاطا مقلية منزلية", "Frites Maison", "French Fries", "cat_rice_sides", 250.0, 80.0, "613000012", "MAIN_KITCHEN", true, "بطاطا مقرمشة طازجة"),
                ProductEntity("p_13", "كوكا كولا زجاج 33cl", "Coca-Cola 33cl", "Coca-Cola 33cl", "cat_drinks", 120.0, 80.0, "613000013", "DRINKS", true, "مشروب غازي كوكا كولا"),
                ProductEntity("p_14", "حمود بوعلام سيليكتو", "Selecto Hamoud Boualem", "Hamoud Boualem Selecto", "cat_drinks", 120.0, 80.0, "613000014", "DRINKS", true, "المشروب الجزائري الأصيل ذوق تفاح كراميل"),
                ProductEntity("p_15", "عصير برتقال طبيعي طازج", "Jus d'Orange Pressé", "Fresh Squeezed Orange Juice", "cat_drinks", 350.0, 150.0, "613000015", "DRINKS", true, "عصير برتقال طبيعي 100% طازج"),
                ProductEntity("p_16", "ماء معدني لالة خديجة", "Eau Lalla Khedidja 0.5L", "Mineral Water 0.5L", "cat_drinks", 80.0, 45.0, "613000016", "DRINKS", true, "قارورة ماء معدني طبيعي"),
                ProductEntity("p_17", "قلب اللوز الجزائري بالعسل", "Kalb El Louz au Miel", "Algerian Kalb El Louz", "cat_dessert", 200.0, 70.0, "613000017", "DESSERT", true, "قطعة قلب اللوز المحشو باللوز والعسل"),
                ProductEntity("p_18", "طاجين الحلو بالمكسرات", "Tajine L'hlou", "Sweet Tajine with Prunes", "cat_dessert", 600.0, 250.0, "613000018", "DESSERT", true, "برقوق ومشمش مجفف مع ماء الزهر واللوز"),
                ProductEntity("p_19", "شاي صحراوي بالنعناع", "Thé à la Menthe", "Saharan Mint Tea", "cat_drinks", 150.0, 40.0, "613000019", "DRINKS", true, "شاي أصيل بالنعناع ورغوة كثيفة")
            )
            dao.insertProducts(products)

            // 4. Tables (12 tables: Main Hall, VIP & Terrace)
            val tables = (1..12).map { num ->
                TableEntity(
                    id = "tbl_$num",
                    tableNumber = num,
                    capacity = if (num in listOf(5, 6, 12)) 6 else if (num == 10) 8 else 4,
                    status = if (num == 1) "OCCUPIED" else if (num == 4) "WAITING_PAYMENT" else "AVAILABLE",
                    currentOrderId = if (num == 1) "ord_demo_1" else null,
                    guestCount = if (num == 1) 3 else 0,
                    notes = if (num in 1..6) "الصالة الرئيسية" else if (num in 7..9) "شرفة المطعم" else "قسم العائلات VIP"
                )
            }
            dao.insertTables(tables)

            // 5. Inventory Items
            val inventoryItems = listOf(
                InventoryItemEntity("inv_chicken", "دجاج بلدي طازج", "Poulet Frais", "ING-001", "kg", 45.0, 12.0, 650.0),
                InventoryItemEntity("inv_meat", "لحم خروف بلدي", "Viande d'Agneau", "ING-002", "kg", 28.0, 8.0, 2100.0),
                InventoryItemEntity("inv_rice", "أرز بسمتي هندي", "Riz Basmati", "ING-003", "kg", 60.0, 15.0, 350.0),
                InventoryItemEntity("inv_chakhchoukha", "رقائق الشخشوخة", "Feuilles Chakhchoukha", "ING-004", "kg", 20.0, 5.0, 400.0),
                InventoryItemEntity("inv_trida", "تريدة تقليدية يدوية", "Pâte Trida", "ING-005", "kg", 18.0, 5.0, 450.0),
                InventoryItemEntity("inv_frik", "فريك أصيل", "Frik Vert", "ING-006", "kg", 15.0, 4.0, 500.0),
                InventoryItemEntity("inv_oil", "زيت طهي نباتي", "Huile Végétale", "ING-007", "liter", 40.0, 10.0, 200.0),
                InventoryItemEntity("inv_coca", "كوكا كولا زجاج 33cl", "Coca-Cola 33cl", "BEV-001", "piece", 96.0, 24.0, 80.0),
                InventoryItemEntity("inv_selecto", "سيليكتو حمود بوعلام", "Selecto 33cl", "BEV-002", "piece", 72.0, 24.0, 80.0),
                InventoryItemEntity("inv_water", "ماء لالة خديجة 0.5L", "Eau 0.5L", "BEV-003", "piece", 120.0, 30.0, 45.0),
                InventoryItemEntity("inv_potatoes", "بطاطا طازجة", "Pommes de Terre", "ING-008", "kg", 55.0, 15.0, 90.0)
            )
            dao.insertInventoryItems(inventoryItems)

            // 6. Recipes
            val recipes = listOf(
                RecipeItemEntity("rec_1", "p_1", "inv_chakhchoukha", 0.25, "kg"),
                RecipeItemEntity("rec_2", "p_1", "inv_chicken", 0.35, "kg"),
                RecipeItemEntity("rec_3", "p_2", "inv_trida", 0.20, "kg"),
                RecipeItemEntity("rec_4", "p_2", "inv_meat", 0.25, "kg"),
                RecipeItemEntity("rec_5", "p_3", "inv_chicken", 0.50, "kg"),
                RecipeItemEntity("rec_6", "p_3", "inv_rice", 0.18, "kg"),
                RecipeItemEntity("rec_7", "p_4", "inv_chicken", 1.00, "kg"),
                RecipeItemEntity("rec_8", "p_7", "inv_meat", 0.35, "kg"),
                RecipeItemEntity("rec_9", "p_7", "inv_potatoes", 0.25, "kg"),
                RecipeItemEntity("rec_10", "p_8", "inv_rice", 0.20, "kg"),
                RecipeItemEntity("rec_11", "p_12", "inv_potatoes", 0.30, "kg"),
                RecipeItemEntity("rec_12", "p_13", "inv_coca", 1.0, "piece"),
                RecipeItemEntity("rec_13", "p_14", "inv_selecto", 1.0, "piece"),
                RecipeItemEntity("rec_14", "p_16", "inv_water", 1.0, "piece")
            )
            dao.insertRecipeItems(recipes)

            // 7. Initial Table 1 Demo Order
            val demoOrder = OrderEntity(
                id = "ord_demo_1",
                orderNumber = "ORD-20260928-0001",
                orderType = "DINE_IN",
                tableId = "tbl_1",
                tableNumber = 1,
                cashierName = "أحمد الجيجلي (كاشير)",
                status = "PREPARING",
                subtotal = 2670.0,
                discount = 0.0,
                tax = 0.0,
                total = 2670.0,
                notes = "دجاج مشوي مستوي جيداً",
                createdAt = System.currentTimeMillis() - 15 * 60 * 1000
            )
            dao.insertOrder(demoOrder)

            val demoItems = listOf(
                OrderItemEntity("oi_1", "ord_demo_1", "p_3", "نصف دجاج مشوي مع الأرز", 850.0, 2, "واحد بدون بصل", "GRILL", "PREPARING"),
                OrderItemEntity("oi_2", "ord_demo_1", "p_8", "صحن أرز بسمتي بالزعفران", 450.0, 1, "", "MAIN_KITCHEN", "READY"),
                OrderItemEntity("oi_3", "ord_demo_1", "p_13", "كوكا كولا زجاج 33cl", 120.0, 2, "باردة", "DRINKS", "READY"),
                OrderItemEntity("oi_4", "ord_demo_1", "p_16", "ماء معدني لالة خديجة", 80.0, 1, "", "DRINKS", "READY")
            )
            dao.insertOrderItems(demoItems)

            // 8. Open Cash Session for today
            val session = CashSessionEntity(
                id = "cs_today",
                cashierName = "أحمد الجيجلي (كاشير)",
                openedAt = System.currentTimeMillis() - 4 * 3600 * 1000,
                openingCash = 20000.0,
                cashSales = 18500.0,
                cashExpenses = 2500.0,
                expectedCash = 36000.0,
                status = "OPEN",
                notes = "افتتاح الصندوق ليوم العمل - وردية الصباح"
            )
            dao.insertCashSession(session)

            // 9. Initial Expense
            val expense = ExpenseEntity(
                id = "exp_1",
                category = "شراء خضار طازجة",
                amount = 2500.0,
                description = "شراء طماطم وفلفل وبصل من سوق الجملة حسين داي",
                userName = "أحمد الجيجلي",
                timestamp = System.currentTimeMillis() - 2 * 3600 * 1000
            )
            dao.insertExpense(expense)

            // 10. Audit Log
            val log = AuditLogEntity(
                id = "aud_init",
                userName = "admin",
                action = "INITIALIZE_SYSTEM",
                details = "تهيئة نظام القصر الذهبي POS وتشغيل قاعدة البيانات بنجاح"
            )
            dao.insertAuditLog(log)
        }
    }
}
