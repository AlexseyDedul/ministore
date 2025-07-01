package by.alexdedul.managerapp.controller;

import by.alexdedul.managerapp.controller.payload.UpdateProductPayload;
import by.alexdedul.managerapp.entity.Product;
import by.alexdedul.managerapp.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("catalogue/products/{productId:\\d+}")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    private final MessageSource messageSource;

    @ModelAttribute("product")
    public Product getProduct(@PathVariable("productId") Integer productId) {
        return productService.findProductById(productId)
                .orElseThrow(() -> new NoSuchElementException("catalogue.errors.product.not_found"));
    }

    @GetMapping
    public String getProductById(){
        return "catalogue/products/product";
    }

    @GetMapping("edit")
    public String editProductPage(){
        return "catalogue/products/edit";
    }

    @PostMapping("edit")
    public String updateProduct(
            @ModelAttribute(value = "product", binding = false) Product product,
            @Valid UpdateProductPayload updateProductPayload,
            BindingResult bindingResult,
            Model model){
        if(bindingResult.hasErrors()){
            model.addAttribute("payload", updateProductPayload);
            model.addAttribute("errors", bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList());
            return "catalogue/products/edit";
        }else {
            productService.updateProduct(product.getId(), updateProductPayload.title(), updateProductPayload.details());
            return "redirect:/catalogue/products/%d".formatted(product.getId());
        }
    }

    @PostMapping("delete")
    public String deleteProduct(@ModelAttribute("product") Product product){
        productService.deleteProduct(product.getId());
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
