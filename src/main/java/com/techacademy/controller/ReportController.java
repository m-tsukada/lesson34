package com.techacademy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Employee.Role;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
 
    // 日報一覧画面
    @GetMapping({ "", "/" })
    public String getReports(Model model, @AuthenticationPrincipal UserDetail userDetail) {

        Employee loggedInUser = userDetail.getEmployee();

        List<Report> reports = null;

        // 管理者の場合は全日報を取得
        if (loggedInUser.getRole() == Role.ADMIN) {
            reports = reportService.findAll();
        } else {
            // 管理者ではない場合は自分の日報を取得
            reports = reportService.findAllByUser(loggedInUser);
        }

        model.addAttribute("listSize", reports.size());
        model.addAttribute("reportList", reports);

        return "reports/list";
    }

    // 日報新規登録画面表示
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        String loggedInEmployeeName = userDetail.getEmployee().getName();
        model.addAttribute("loggedInEmployeeName", loggedInEmployeeName);

        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        // 入力チェック（名前の文字数などentityで規定したものへのエラー）
        if (res.hasErrors()) {
            return create(report, userDetail, model);
        }

        Employee loggedInEmployeeCode = userDetail.getEmployee();
        report.setEmployee(loggedInEmployeeCode);

        // ログインしているユーザーと日報提出日付
        List<Report> reports = reportService.findByEmployeeAndReportDate(loggedInEmployeeCode, report.getReportDate());

        if (!reports.isEmpty()) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR), ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
            return create(report, userDetail, model);
        }

        ErrorKinds result = reportService.save(report);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return create(report, userDetail, model);
        }

        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable("id") Integer id, Model model) {
        
        model.addAttribute("report", reportService.findById(id));
        return "reports/detail";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {

/*         Employee loggedInEmployee = userDetail.getEmployee();
        Report report = reportService.findById(id);

        if (report.getEmployee().getCode() != loggedInEmployee.getCode()) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DELETE_ERROR), ErrorMessage.getErrorValue(ErrorKinds.DELETE_ERROR));
            return detail(id, model);
        } */

        ErrorKinds result = reportService.delete(id, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findById(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }

    // 日報更新画面表示
    @GetMapping(value = "/{id}/update")
    public String update(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model, Report rep) {

        String loggedInEmployeeName = userDetail.getEmployee().getName();
        model.addAttribute("loggedInEmployeeName", loggedInEmployeeName);

        if (id != null) {
            Report report = reportService.findById(id);
            model.addAttribute("report", report);
        } else {
            model.addAttribute("report", rep);
        }
        return "reports/update";
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable Integer id, @Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        
        // 入力チェック（名前の文字数などentityで規定したものへのエラー）
        if (res.hasErrors()) {
            return update(null, userDetail, model, report);
        }

        // ログインしていれば、日報にセットする
        Employee loggedInEmployeeCode = userDetail.getEmployee();
        report.setEmployee(loggedInEmployeeCode);

        // 更新前と更新後の日付をチェック
        Report existReport = reportService.findById(id);
        // 日付が同じならエラーではない
        if (existReport != null && existReport.getReportDate().equals(report.getReportDate())) {
            // エラーではない
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR), null);            
        } else {
            // すでに登録された日付の場合エラー
            List<Report> reports = reportService.findByEmployeeAndReportDate(loggedInEmployeeCode, report.getReportDate());
            if (!reports.isEmpty()) {
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR), ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
                return update(id, userDetail, model, report);
            }
        }

        ErrorKinds result = reportService.update(report);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return update(id, userDetail, model, report);
        }

        return "redirect:/reports";
    }
}
