package by.alexdedul.managerapp.controller;

import by.alexdedul.managerapp.client.ProductsRestClient;
import by.alexdedul.managerapp.client.exception.BadRequestException;
import by.alexdedul.managerapp.controller.payload.UpdateProductPayload;
import by.alexdedul.managerapp.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("catalogue/products/{productId:\\d+}")
@RequiredArgsConstructor
public class ProductController {
    private final ProductsRestClient productsRestClient;

    private final MessageSource messageSource;

    @ModelAttribute("product")
    public Product getProductById(@PathVariable("productId") Integer productId) {
        return productsRestClient.getProduct(productId)
                .orElseThrow(() -> new NoSuchElementException("catalogue.errors.product.not_found"));
    }

    @GetMapping
    public String getProduct(){
        return "catalogue/products/product";
    }

    @GetMapping("edit")
    public String editProductPage(){
        return "catalogue/products/edit";
    }

    @PostMapping("edit")
    public String updateProduct(
            @ModelAttribute(value = "product", binding = false) Product product,
            UpdateProductPayload updateProductPayload,
            Model model,
            HttpServletResponse response){
        try{
            productsRestClient.updateProduct(product.id(), updateProductPayload.title(), updateProductPayload.details());
            return "redirect:/catalogue/products/%d".formatted(product.id());
        }catch (BadRequestException e){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("payload", updateProductPayload);
            model.addAttribute("errors", e.getErrors());
            return "catalogue/products/edit";
        }
    }

    @PostMapping("delete")
    public String deleteProduct(@ModelAttribute("product") Product product){
        productsRestClient.deleteProduct(product.id());
        return "redirect:/catalogue/products/list";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(
            NoSuchElementException ex,
            Model model,
            HttpServletResponse response,
            Locale locale){
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("error", messageSource.getMessage(ex.getMessage(), new Object[0], ex.getMessage(), locale));
        return "errors/404";
    }

}
