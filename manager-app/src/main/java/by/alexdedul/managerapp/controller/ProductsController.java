package by.alexdedul.managerapp.controller;

import by.alexdedul.managerapp.controller.payload.NewProductPayload;
import by.alexdedul.managerapp.entity.Product;
import by.alexdedul.managerapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("catalogue/products")
public class ProductsController {
    private final ProductService productService;

    @GetMapping(value = "list")
    public String getProductList(Model model) {
        model.addAttribute("products", productService.findAllProducts());
        return "catalogue/products/list";
    }

    @GetMapping("create")
    public String getNewProductPage(){
        return "catalogue/products/new_product";
    }

    @PostMapping("create")
    public String createProduct(@Valid NewProductPayload product,
                                BindingResult bindingResult,
                                Model model) {
        if(bindingResult.hasErrors()){
            model.addAttribute("payload", product);
            model.addAttribute("errors", bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList());
            return "catalogue/products/new_product";
        }else {
            Product newProduct = this.productService.createProduct(product.title(), product.details());
            return "redirect:/catalogue/products/%d".formatted(newProduct.getId());
        }
    }
}
