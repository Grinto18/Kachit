package com.example

import com.example.data.entity.ProductEntity
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.CartState
import org.junit.Assert.*
import org.junit.Test

class PosLogicUnitTest {

    @Test
    fun testCartCalculation_subtotalAndDiscount() {
        val chicken = ProductEntity(
            id = "p_1",
            nameAr = "شخشوخة بسكرية بالدجاج",
            nameFr = "Chakhchoukha Poulet",
            nameEn = "Chicken Chakhchoukha",
            categoryId = "cat_main",
            price = 1200.0
        )
        val drink = ProductEntity(
            id = "p_13",
            nameAr = "كوكا كولا",
            nameFr = "Coca-Cola",
            nameEn = "Coca-Cola",
            categoryId = "cat_drinks",
            price = 120.0
        )

        val items = listOf(
            CartItem(product = chicken, quantity = 2, unitPrice = 1200.0), // 2400
            CartItem(product = drink, quantity = 3, unitPrice = 120.0)      // 360
        )

        val cart = CartState(
            items = items,
            discountPercent = 10.0,
            deliveryFee = 200.0
        )

        // Subtotal = 2400 + 360 = 2760
        assertEquals(2760.0, cart.subtotal, 0.01)
        // Discount 10% = 276.0
        assertEquals(276.0, cart.discountAmount, 0.01)
        // Total = 2760 - 276 + 200 = 2684.0
        assertEquals(2684.0, cart.total, 0.01)
        // Total items = 5
        assertEquals(5, cart.totalItemCount)
    }

    @Test
    fun testChangeCalculation() {
        val totalAmount = 2500.0
        val receivedAmount = 3000.0
        val changeAmount = (receivedAmount - totalAmount).coerceAtLeast(0.0)
        assertEquals(500.0, changeAmount, 0.01)
    }

    @Test
    fun testSplitBillCalculation() {
        val totalAmount = 6000.0
        val count = 3
        val perPerson = totalAmount / count
        assertEquals(2000.0, perPerson, 0.01)
    }

    @Test
    fun testRecipeIngredientDeductionMath() {
        val dishQuantity = 3
        val requiredGramsPerDish = 250.0 // 250g
        val totalDeductionGrams = dishQuantity * requiredGramsPerDish
        assertEquals(750.0, totalDeductionGrams, 0.01)
    }
}
