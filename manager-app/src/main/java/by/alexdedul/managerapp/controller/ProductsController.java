package by.alexdedul.managerapp.controller;

import by.alexdedul.managerapp.client.ProductsRestClient;
import by.alexdedul.managerapp.client.exception.BadRequestException;
import by.alexdedul.managerapp.controller.payload.NewProductPayload;
import by.alexdedul.managerapp.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("catalogue/products")
public class ProductsController {
    private final ProductsRestClient productsRestClient;

    @GetMapping(value = "list")
    public String getProductList(Model model) {
        model.addAttribute("products", productsRestClient.getProducts());
        return "catalogue/products/list";
    }

    @GetMapping("create")
    public String getNewProductPage(){
        return "catalogue/products/new_product";
    }

    @PostMapping("create")
    public String createProduct(NewProductPayload product,
                                Model model) {
        try {
            Product newProduct = this.productsRestClient.createProduct(product.title(), product.details());
            return "redirect:/catalogue/products/%d".formatted(newProduct.id());
        }catch (BadRequestException e){
            model.addAttribute("payload", product);
            model.addAttribute("errors", e.getErrors());
            return "catalogue/products/new_product";
        }
    }
}
