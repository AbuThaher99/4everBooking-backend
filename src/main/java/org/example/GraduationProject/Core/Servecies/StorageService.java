package org.example.GraduationProject.Core.Servecies;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import lombok.AllArgsConstructor;
import org.example.GraduationProject.Common.Entities.FileData;
import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.Reservations;
import org.example.GraduationProject.Core.Repsitories.FileDataRepository;
import org.example.GraduationProject.Core.Repsitories.HallOwnerRepository;
import org.example.GraduationProject.Core.Repsitories.HallRepository;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

@Service
@AllArgsConstructor
public class StorageService {

    private final FileDataRepository fileDataRepository;
    private final HallRepository hallRepository;
    private final HallOwnerRepository hallOwnerRepository;

    final String baseUrl = "https://firebasestorage.googleapis.com/v0/b/graduationproject-df4b7.appspot.com/o/";

    public String uploadInvoiceForReservation(Reservations reservations,String billNumber ) {
        String fileName = "Invoice_" + reservations.getCustomer().getId() + "_" + System.currentTimeMillis() + ".pdf";


        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Set fonts for titles and text
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            // Add title to the PDF
            Paragraph title = new Paragraph("Invoice for Reservation", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n")); // Add spacing

            // Add bill number
            document.add(new Paragraph("Bill Number: " + billNumber, boldFont));
            document.add(new Paragraph("Reservation Date: " + reservations.getDate(), normalFont));

            // Handle end date (can be null)
            if (reservations.getEndDate() != null) {
                document.add(new Paragraph("End Date: " + reservations.getEndDate(), normalFont));
            } else {
                document.add(new Paragraph("End Date: N/A", normalFont));
            }

            // Add customer details
            document.add(new Paragraph("Customer: " + reservations.getCustomer().getUser().getFirstName() + " " + reservations.getCustomer().getUser().getLastName(), normalFont));
            document.add(new Paragraph("\n")); // Add spacing

            // Add total price
            document.add(new Paragraph("Total Price: " + reservations.getTotalPrice() + " NIC", boldFont));
            document.add(new Paragraph("\n")); // Add spacing

            // Add chosen services in a table format
            document.add(new Paragraph("Chosen Services:", boldFont));
            PdfPTable table = new PdfPTable(2); // 2 columns: Service Name and Price
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Table headers
            PdfPCell header1 = new PdfPCell(new Paragraph("Service Name", boldFont));
            PdfPCell header2 = new PdfPCell(new Paragraph("Price (NIS)", boldFont));
            table.addCell(header1);
            table.addCell(header2);

            // Populate the table with services
            for (Map.Entry<String, Object> service : reservations.getChosenServices().entrySet()) {
                table.addCell(new PdfPCell(new Paragraph(service.getKey(), normalFont))); // Service name
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(service.getValue()), normalFont))); // Price
            }

            document.add(table); // Add table to the PDF

            document.close(); // Close the document

            // Upload the generated PDF to cloud storage
            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.create("uploads/Invoice/" + fileName, outputStream.toByteArray(), "application/pdf");

            // Save file data with proper URL encoding for the file path
            String fileUrl = baseUrl + "uploads%2FInvoice%2F" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()) + "?alt=media";
            fileDataRepository.save(FileData.builder()
                    .name(fileName)
                    .type("application/pdf")
                    .filePath(fileUrl)
                    .build());

        } catch (DocumentException | IOException e) {
            e.printStackTrace(); // Handle exceptions properly
        }

        return fileName;
    }


    public String uploadImageToFileProfile(MultipartFile file) throws IOException {
        StringBuilder fileUrl = new StringBuilder();

        String fileNameWithTimestamp = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Bucket bucket = StorageClient.getInstance().bucket();

        Blob blob = bucket.create("uploads/ProfileImages/" + fileNameWithTimestamp, file.getBytes(), file.getContentType());

        String fullFileUrl = baseUrl + "uploads%2FProfileImages%2F" + fileNameWithTimestamp + "?alt=media";

        fileUrl.append(fullFileUrl);

        return fileUrl.toString();
    }

    public String uploadProveHall(MultipartFile file) throws IOException {
        StringBuilder fileUrl = new StringBuilder();

        String fileNameWithTimestamp = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Bucket bucket = StorageClient.getInstance().bucket();

        Blob blob = bucket.create("uploads/proofHalls/" + fileNameWithTimestamp, file.getBytes(), file.getContentType());

        String fullFileUrl = baseUrl + "uploads%2FproofHalls%2F" + fileNameWithTimestamp + "?alt=media";

        fileUrl.append(fullFileUrl);

        return fileUrl.toString();
    }

    public FileData downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<FileData> fileData = fileDataRepository.findByName(fileName);
        if (fileData.isPresent()) {
            String fileUrl = fileData.get().getFilePath();
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                byte[] imageData = out.toByteArray();
                fileData.get().setData(imageData);
                return fileData.get();
            }
        }
        throw new IOException("File not found");
    }

    public String uploadMultiImageToFileSystem(MultipartFile[] files) throws IOException {
        StringBuilder fileUrls = new StringBuilder();
        Set<String> uploadedFiles = new HashSet<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String fileNameWithTimestamp = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            if (uploadedFiles.contains(fileNameWithTimestamp)) {
                continue;
            }

            Bucket bucket = StorageClient.getInstance().bucket();

            Blob blob = bucket.create("uploads/HallImage/" + fileNameWithTimestamp, file.getBytes(), file.getContentType());

            String fileUrl = baseUrl + "uploads%2FHallImage%2F" + fileNameWithTimestamp + "?alt=media";

            fileUrls.append(fileUrl);
            uploadedFiles.add(fileNameWithTimestamp);

            if (i < files.length - 1) {
                fileUrls.append(", ");
            }
        }

        return fileUrls.toString();
    }


    public String addNewImage(Long hallId, MultipartFile[] images) throws IOException {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
        List<String> newImageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            String imageUrl = uploadMultiImageToFirebase(new MultipartFile[]{image});
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                newImageUrls.add(imageUrl);
            }
        }
        String newImagesUrlString = String.join(",", newImageUrls);

        String currentImages = hall.getImage();

        if (currentImages == null || currentImages.isEmpty()) {
            currentImages = newImagesUrlString;
        } else if (!newImagesUrlString.isEmpty()) {
            currentImages = currentImages + "," + newImagesUrlString;
        }
        currentImages = Arrays.stream(currentImages.split(","))
                .filter(url -> !url.trim().isEmpty())
                .collect(Collectors.joining(","));

        hall.setImage(currentImages);
        hallRepository.save(hall);

        return currentImages;
    }

    public void deleteImage(Long hallId, String imageUrl) throws IOException {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));

        String currentImages = hall.getImage();

        String decodedImageUrl = java.net.URLDecoder.decode(imageUrl, "UTF-8");
        String decodedCurrentImages = java.net.URLDecoder.decode(currentImages, "UTF-8");

        if (decodedCurrentImages.contains(decodedImageUrl)) {
            String updatedImages = removeImageFromString(decodedCurrentImages, decodedImageUrl);
            hall.setImage(updatedImages);
            hallRepository.save(hall);

            deleteFileFromFirebase(decodedImageUrl);
        } else {
            throw new IllegalArgumentException("Image not found in hall images");
        }
    }

    private String removeImageFromString(String images, String imageUrl) {
        String[] imageArray = images.split(",");
        StringBuilder updatedImages = new StringBuilder();

        for (String img : imageArray) {
            if (!img.trim().equals(imageUrl.trim())) {
                if (updatedImages.length() > 0) {
                    updatedImages.append(",");
                }
                updatedImages.append(img);
            }
        }

        return updatedImages.toString();
    }

    public void deleteFileFromFirebase(String imageUrl) {
        try {
            String filePath = imageUrl.replace(baseUrl, "").replaceAll("%2F", "/").split("\\?")[0];

            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.get(filePath);

            if (blob != null && blob.exists()) {
                boolean deleted = blob.delete();
                if (deleted) {
                    System.out.println("File successfully deleted from Firebase.");
                } else {
                    System.out.println("Failed to delete the file from Firebase.");
                }
            } else {
                System.out.println("Blob not found in Firebase.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while trying to delete the file from Firebase.");
        }
    }


    public String uploadMultiImageToFirebase(MultipartFile[] files) throws IOException {
        StringBuilder fileUrls = new StringBuilder();

        for (MultipartFile file : files) {
            String fileNameWithTimestamp = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Get Firebase Storage Bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Upload the image to Firebase Cloud Storage
            Blob blob = bucket.create("uploads/HallImage/" + fileNameWithTimestamp, file.getBytes(), file.getContentType());

            // Generate Firebase file URL
            String fileUrl = baseUrl + "uploads%2FHallImage%2F" + fileNameWithTimestamp + "?alt=media";

            fileUrls.append(fileUrl);

            if (fileUrls.length() > 0) {
                fileUrls.append(", ");
            }
        }

        return fileUrls.toString();
    }

    public class BackgroundPageEvent extends PdfPageEventHelper {

        private Image backgroundImage;

        public BackgroundPageEvent() {
            try {
                InputStream imageStream = getClass().getClassLoader().getResourceAsStream("background.jpg");
                if (imageStream == null) {
                    throw new RuntimeException("Background image not found");
                }
                backgroundImage = Image.getInstance(imageStream.readAllBytes());

                backgroundImage.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                backgroundImage.setAbsolutePosition(0, 0); // Set position to (0, 0) of the page
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte canvas = writer.getDirectContentUnder();
                canvas.addImage(backgroundImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String generateMonthlyReservationChart(Long ownerId) throws IOException {
        List<Reservations> reservations = hallRepository.findAllbyHallOwner(ownerId);
        if (reservations.isEmpty()) {
            throw new IOException("No reservations found for the given owner ID.");
        }

        Map<String, Double> monthlyTotal = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        for (int month = 1; month <= 12; month++) {
            String key = calendar.get(Calendar.YEAR) + "-" + month;
            monthlyTotal.put(key, 0.0);
        }

        for (Reservations reservation : reservations) {
            calendar.setTime(reservation.getCreatedDate());
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);

            String key = year + "-" + month;
            monthlyTotal.put(key, monthlyTotal.getOrDefault(key, 0.0) + reservation.getTotalPrice());
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : monthlyTotal.entrySet()) {
            dataset.addValue(entry.getValue(), "Total Price", entry.getKey());
        }

        // Create the bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Reservations",
                "Month",
                "Total Price",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        plot.setDomainAxis(new CategoryAxis("Month"));
        plot.setRangeAxis(new NumberAxis("Total Amount"));

        plot.getRangeAxis().setLabelFont(new java.awt.Font("SansSerif", Font.BOLD, 10));
        plot.getRangeAxis().setTickLabelFont(new java.awt.Font("SansSerif", Font.BOLD, 14));

        CustomBarRenderer renderer = new CustomBarRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
        renderer.setDrawBarOutline(false);

        plot.setRenderer(renderer);
        chart.setBorderVisible(false);
        chart.getLegend().setFrame(BlockBorder.NONE);

        String chartFileName = "MonthlyReservations_" + ownerId + "_" + System.currentTimeMillis() + ".png";

        // Convert chart to PNG and upload to Firebase
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtilities.writeChartAsPNG(outputStream, chart, 800, 600);

        // Upload the PNG chart to Firebase Cloud Storage
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create("uploads/Chart/" + chartFileName, outputStream.toByteArray(), "image/png");

        return baseUrl + "uploads%2FChart%2F" + chartFileName + "?alt=media";
    }

    class CustomBarRenderer extends BarRenderer {
        private final Color[] colors = {
                Color.decode("#FF0000"), // Jan
                Color.decode("#FFFF00"), // Feb
                Color.decode("#00FF00"), // Mar
                Color.decode("#FFC0CB"), // Apr
                Color.decode("#0000FF"), // May
                Color.decode("#A52A2A"), // Jun
                Color.decode("#C7FF33"), // Jul
                Color.decode("#CA33FF"), // Aug
                Color.decode("#FF336B"), // Sep
                Color.decode("#33FFE3"), // Oct
                Color.decode("#E0FF33"), // Nov
                Color.decode("#C433FF")  // Dec
        };

        @Override
        public Paint getItemPaint(int row, int column) {
            return colors[column % colors.length];
        }
    }

    public String generateHallReservationReport(Long ownerId, String headerHex, String evenRowHex, String oddRowHex) throws UserNotFoundException, IOException, DocumentException {
        hallOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Owner not found"));

        BaseColor headerColor = hexToBaseColor(headerHex);
        BaseColor evenRowColor = hexToBaseColor(evenRowHex);
        BaseColor oddRowColor = hexToBaseColor(oddRowHex);

        String fileName = "HallReport_" + ownerId + "_" + System.currentTimeMillis() + ".pdf";

        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        writer.setPageEvent(new HeaderFooterPageEvent());
        writer.setPageEvent(new BackgroundPageEvent());
        document.open();

        addCoverPage(document);

        List<Hall> halls = hallRepository.findByOwnerId(ownerId);
        if (halls.isEmpty()) {
            throw new DocumentException("No halls found for the given owner ID.");
        }

        double grandTotalPrice = 0;

        for (Hall hall : halls) {
            addHallDetails(document, hall);

            List<Reservations> reservations = hallRepository.findByHallId(hall.getId());
            if (reservations.isEmpty()) {
                document.add(new Paragraph("No reservations found for this hall.", new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.ITALIC)));
            } else {

                document.add(Chunk.NEWLINE);

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2, 2, 2, 2});

                addTableHeader(table, headerColor, "Reservation ID", "Start Date", "End Date", "Total Price");

                double totalPriceForHall = 0;
                boolean alternate = false;
                for (Reservations reservation : reservations) {
                    String endDate = reservation.getEndDate() != null ? reservation.getEndDate().toString() : "N/A";
                    addTableRow(table,
                            String.valueOf(reservation.getId()),
                            reservation.getDate().toString(),
                            endDate,
                            String.valueOf(reservation.getTotalPrice()),
                            alternate, evenRowColor, oddRowColor);

                    totalPriceForHall += reservation.getTotalPrice();
                    alternate = !alternate;
                }

                document.add(table);

                Font totalPriceFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
                Paragraph totalPrice = new Paragraph("Total Price for Hall: $" + totalPriceForHall, totalPriceFont);
                totalPrice.setAlignment(Element.ALIGN_RIGHT);
                document.add(totalPrice);

                grandTotalPrice += totalPriceForHall;
            }

            document.add(Chunk.NEWLINE);
        }

        addSummarySection(document, grandTotalPrice);

        // Add chart image to the report
        String chartFileUrl = generateMonthlyReservationChart(ownerId);
        Image chartImage = Image.getInstance(new URL(chartFileUrl));
        chartImage.scaleToFit(PageSize.A4.getWidth() - 72, PageSize.A4.getHeight() - 72);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);

        document.close();

        // Upload the PDF to Firebase Cloud Storage
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create("uploads/Pdf/" + fileName, outputStream.toByteArray(), "application/pdf");

        fileDataRepository.save(FileData.builder()
                .name(fileName)
                .type("application/pdf")
                .filePath(baseUrl + "uploads%2FPdf%2F" + fileName + "?alt=media")
                .build());

        return baseUrl + "uploads%2FPdf%2F" + fileName + "?alt=media";
    }

    private BaseColor hexToBaseColor(String hex) {
        Color awtColor = Color.decode(hex);
        return new BaseColor(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    private void addCoverPage(Document document) throws DocumentException {
        document.newPage();

        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD);
        Paragraph title = new Paragraph("Hall Reservations Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph date = new Paragraph("Generated on: " + new java.util.Date().toString(), new Font(Font.FontFamily.TIMES_ROMAN, 12));
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
    }

    private void addHallDetails(Document document, Hall hall) throws DocumentException {
        Font hallFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        Paragraph hallName = new Paragraph("Hall Name: " + hall.getName(), hallFont);
        document.add(hallName);

        Paragraph hallLocation = new Paragraph("Location: " + hall.getLocation());
        document.add(hallLocation);
    }

    private void addSummarySection(Document document, double grandTotalPrice) throws DocumentException {
        Font summaryFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        Paragraph summaryTitle = new Paragraph("Summary", summaryFont);
        document.add(summaryTitle);

        Paragraph grandTotal = new Paragraph("Grand Total Price for All Halls: $" + grandTotalPrice, summaryFont);
        grandTotal.setAlignment(Element.ALIGN_RIGHT);
        document.add(grandTotal);
    }

    private void addTableHeader(PdfPTable table, BaseColor headerColor, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(headerColor);
            cell.setPhrase(new Phrase(header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String id, String startDate, String endDate, String totalPrice, boolean alternate, BaseColor evenRowColor, BaseColor oddRowColor) {
        PdfPCell[] cells = new PdfPCell[]{
                new PdfPCell(new Phrase(id)),
                new PdfPCell(new Phrase(startDate)),
                new PdfPCell(new Phrase(endDate)),
                new PdfPCell(new Phrase(totalPrice))
        };

        BaseColor rowColor = alternate ? evenRowColor : oddRowColor;
        for (PdfPCell cell : cells) {
            cell.setBackgroundColor(rowColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    public class HeaderFooterPageEvent extends PdfPageEventHelper {
        private final Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        private final Font footerFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable headerTable = new PdfPTable(1);
            PdfPTable footerTable = new PdfPTable(1);
            headerTable.setTotalWidth(523);
            headerTable.setLockedWidth(true);
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            headerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(new Phrase("Hall Reservations Report", headerFont));
            headerTable.writeSelectedRows(0, -1, 36, 820, writer.getDirectContent());

            footerTable.setTotalWidth(523);
            footerTable.setLockedWidth(true);
            footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            footerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(new Phrase("Page " + writer.getPageNumber(), footerFont));
            footerTable.writeSelectedRows(0, -1, 36, 30, writer.getDirectContent());
        }
    }

}


