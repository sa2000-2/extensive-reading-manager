package com.example.extensivereading.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.extensivereading.dto.BookRecordForm;
import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.UserRepository;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.BookRecordService;

@Controller
@RequestMapping("/books")
public class BookRecordController {
	private final BookRecordService bookRecordService;
    private final UserRepository userRepository;

    public BookRecordController(BookRecordService bookRecordService, UserRepository userRepository) {
        this.bookRecordService = bookRecordService;
        this.userRepository = userRepository;
    }
    

    @GetMapping("/list")
    public String showList(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        String userId = userDetails.getUsername();
        User user = userRepository.findById(userId).orElseThrow();
        int targetWords = user.getTargetWords();
        
        List<BookRecord> records = bookRecordService.getAllRecords(userId);
        int totalWords = bookRecordService.calculateTotalWords(records);
        double progress = bookRecordService.calculateProgressPercentage(totalWords, targetWords);

        model.addAttribute("records", records);
        model.addAttribute("totalWords", totalWords);
        model.addAttribute("targetWords", targetWords);
        model.addAttribute("progress", progress);

        return "bookRecordList";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("bookRecordForm", new BookRecordForm());
        return "bookRecordAdd"; 
    }


    @PostMapping("/add")
    public String addRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
                            @Validated BookRecordForm bookRecordForm,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "bookRecordAdd"; 
        }

        bookRecordService.bookRegister(userDetails.getUsername(), bookRecordForm);

        redirectAttributes.addFlashAttribute("successMessage", "読書記録を登録しました。続けて別の本を登録できます。");
        return "redirect:/books/add"; //

}
    

    @GetMapping("/edit/{recordId}")
    public String showEditForm(@AuthenticationPrincipal UserDetailsImpl userDetails, 
                               @PathVariable Integer recordId, 
                               Model model) {
        
        try {
            BookRecord record = bookRecordService.getRecord(userDetails.getUsername(), recordId);

            BookRecordForm form = new BookRecordForm();
            form.setRecordId(record.getRecordId());
            form.setBookTitle(record.getBookTitle());
            form.setReadDate(record.getReadDate());
            form.setWordCount(record.getWordCount());

            model.addAttribute("bookRecordForm", form);
            return "bookRecordEdit"; 
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return "redirect:/books/list";
        }
    }


    @PostMapping("/update")
    public String editRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @Validated BookRecordForm bookRecordForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "bookRecordEdit"; 
        }

        try {
            bookRecordService.updateRecord(userDetails.getUsername(), bookRecordForm);
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return "redirect:/books/list";
        }

        redirectAttributes.addFlashAttribute("successMessage", "読書記録を更新しました。");
        return "redirect:/books/list";
    }

    @PostMapping("/delete/{recordId}")
    public String deleteRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @PathVariable Integer recordId, 
                               RedirectAttributes redirectAttributes) {
        
    	try {
            bookRecordService.deleteRecord(userDetails.getUsername(), recordId);
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return "redirect:/books/list";
        }

        redirectAttributes.addFlashAttribute("successMessage", "読書記録を削除しました。");
        return "redirect:/books/list";
    }
}
