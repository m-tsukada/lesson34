package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;

import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {
    
    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 日報の一覧表示
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 一般ユーザーが作成したレポートのみを取得
    public List<Report> findAllByUser(Employee employee) {
        return reportRepository.findByEmployee(employee);
    }

    // 新規登録
    @Transactional
    public ErrorKinds save(Report report) {
        report.setDeleteFlg(false);

        // 作成日時、更新日時
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報を書いた従業員とその提出日を検索
    public List<Report> findByEmployeeAndReportDate(Employee employee, LocalDate reportDate) {
        return reportRepository.findByEmployeeAndReportDate(employee, reportDate);
    }

    // 1件を検索
    public Report findById(Integer id) {
        Optional<Report> option = reportRepository.findById(id);
        Report report = option.orElse(null);
        return report;
    }

    // 日報削除処理
    @Transactional
    public ErrorKinds delete(Integer id, UserDetail userDetail) {
        Report report = findById(id);
        report.setDeleteFlg(true);
        report.setUpdatedAt(LocalDateTime.now());
        //report.setUpdatedBy(userDetail.getEmployee());
        
        return ErrorKinds.SUCCESS;
    }

    // 日報更新処理
    
    public ErrorKinds update(Report report) {

        report.setDeleteFlg(false);

        // 作成日時は登録日付
        Report existReport = findById(report.getId());
        report.setCreatedAt(existReport.getCreatedAt());
        
        // 更新日時
        report.setUpdatedAt(LocalDateTime.now());

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }



}
