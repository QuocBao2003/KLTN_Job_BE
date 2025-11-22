package com.example.demo.service;

import com.example.demo.domain.Cv;
import com.example.demo.domain.User;
import com.example.demo.repository.CvRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.error.IdInvalidException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class ExcelService {

    private final CvRepository cvRepository;
    private final UserRepository userRepository;

    public ExcelService(CvRepository cvRepository, UserRepository userRepository) {
        this.cvRepository = cvRepository;
        this.userRepository = userRepository;
    }

    /**
     * Xử lý upload file Excel và tạo CV
     */
    public List<Cv> uploadExcelCv(MultipartFile file) throws IdInvalidException, IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IdInvalidException("File không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        && !contentType.equals("application/vnd.ms-excel"))) {
            throw new IdInvalidException("File phải là định dạng Excel (.xlsx hoặc .xls)");
        }

        List<Cv> cvList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Bỏ qua header row
            if (rows.hasNext()) {
                rows.next();
            }

            // Lấy user hiện tại
            User currentUser = null;
            Optional<String> currentUserLogin = SecurityUtil.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                currentUser = userRepository.findByEmail(currentUserLogin.get()).orElse(null);
            }

            // Đọc từng row
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Bỏ qua row trống
                if (isRowEmpty(currentRow)) {
                    continue;
                }

                Cv cv = new Cv();

                // Column 0: Full Name
                cv.setFullName(getCellValueAsString(currentRow.getCell(0)));

                // Column 1: Email
                cv.setEmail(getCellValueAsString(currentRow.getCell(1)));

                // Column 2: Phone
                cv.setPhone(getCellValueAsString(currentRow.getCell(2)));

                // Column 3: Address
                cv.setAddress(getCellValueAsString(currentRow.getCell(3)));

                // Column 4: Objective
                cv.setObjective(getCellValueAsString(currentRow.getCell(4)));

                // Column 5: Experience
                cv.setExperience(getCellValueAsString(currentRow.getCell(5)));

                // Column 6: Education
                cv.setEducation(getCellValueAsString(currentRow.getCell(6)));

                // Column 7: Skills
                cv.setSkills(getCellValueAsString(currentRow.getCell(7)));

                // Column 8: Photo URL (optional)
                cv.setPhotoUrl(getCellValueAsString(currentRow.getCell(8)));

                // Column 9: CV Template (optional)
                cv.setCvTemplate(getCellValueAsString(currentRow.getCell(9)));

                // Set user
                if (currentUser != null) {
                    cv.setUser(currentUser);
                }

                // Validate required fields
                if (cv.getFullName() == null || cv.getFullName().trim().isEmpty()) {
                    throw new IdInvalidException("Họ tên không được để trống ở row " + (currentRow.getRowNum() + 1));
                }
                if (cv.getEmail() == null || cv.getEmail().trim().isEmpty()) {
                    throw new IdInvalidException("Email không được để trống ở row " + (currentRow.getRowNum() + 1));
                }

                // Save CV
                Cv savedCv = cvRepository.save(cv);
                cvList.add(savedCv);
            }

        } catch (IOException e) {
            throw new IOException("Lỗi khi đọc file Excel: " + e.getMessage());
        }

        return cvList;
    }

    /**
     * Chuyển cell value sang String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Convert number to string without decimal if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * Kiểm tra row có trống không
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}