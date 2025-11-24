package gighub.worketserver.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.User;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PdfGenerationService {

  // 한국어 폰트 경로
  private static final String KOR_FONT_PATH = "/fonts/NanumGothic.ttf";

  /**
   * Contract Entity 기반 PDF 생성
   */
  public byte[] generateContractPdf(Contract contract) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 50, 50, 50, 50);

    try {
      PdfWriter.getInstance(document, baos);
      document.open();

      // 1. BaseFont 준비
      BaseFont baseFont;
      try {
        baseFont = BaseFont.createFont(KOR_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
      } catch (DocumentException e) {
        throw new IOException("한국어 폰트 파일을 찾을 수 없습니다: " + KOR_FONT_PATH, e);
      }

      // 2. Font 준비
      Font titleFont = new Font(baseFont, 20, Font.BOLD);
      Font articleTitleFont = new Font(baseFont, 12, Font.BOLD);
      Font normalFont = new Font(baseFont, 10, Font.NORMAL);
      Font headerFont = new Font(baseFont, 10, Font.BOLD, Color.WHITE);

      NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.KOREA);

      // 제목
      Paragraph mainTitle = new Paragraph(contract.getTitle(), titleFont);
      mainTitle.setAlignment(Element.ALIGN_CENTER);
      mainTitle.setSpacingAfter(24f);
      document.add(mainTitle);


      // 제1조 – 당사자 정보 테이블
      document.add(new Paragraph("제 1 조 (계약 당사자)", articleTitleFont));
      document.add(new Paragraph("본 계약은 아래 당사자 간에 체결된다.\n", normalFont));

      PdfPTable partyTable = new PdfPTable(4);
      partyTable.setWidthPercentage(100);
      partyTable.setSpacingBefore(5f);
      partyTable.setWidths(new int[]{1, 2, 2, 3});

      // 테이블 헤더
      partyTable.addCell(createHeaderCell("구분", headerFont));
      partyTable.addCell(createHeaderCell("이름 / 회사명", headerFont));
      partyTable.addCell(createHeaderCell("역할", headerFont));
      partyTable.addCell(createHeaderCell("연락처", headerFont));

      // 갑(의뢰인)
      User client = contract.getClient();
      partyTable.addCell(createContentCell("갑 (의뢰인)", Element.ALIGN_CENTER, normalFont));
      partyTable.addCell(createContentCell(contract.getClientName(), Element.ALIGN_LEFT, normalFont));
      partyTable.addCell(createContentCell(client.getRole().getKorName() , Element.ALIGN_CENTER, normalFont));
      partyTable.addCell(createContentCell(client.getPhone() , Element.ALIGN_LEFT, normalFont));

      // 을(프리랜서)
      User freelancer = contract.getFreelancer();
      partyTable.addCell(createContentCell("을 (프리랜서)", Element.ALIGN_CENTER, normalFont));
      partyTable.addCell(createContentCell(freelancer.getName(), Element.ALIGN_LEFT, normalFont));
      partyTable.addCell(createContentCell(freelancer.getRole().getKorName() , Element.ALIGN_CENTER, normalFont));
      partyTable.addCell(createContentCell(freelancer.getPhone() , Element.ALIGN_LEFT, normalFont));

      document.add(partyTable);
      document.add(new Paragraph("\n"));

      // 제2조
      document.add(new Paragraph("제 2 조 (계약 개요)", articleTitleFont));
      document.add(new Paragraph(
        "1. 계약 기간: 본 계약은 " + contract.getStartDate() + "부터 " + contract.getEndDate() + "까지 유효하다.",
        normalFont
      ));
      document.add(new Paragraph(
        "2. 계약 금액: 총 " + currencyFormat.format(contract.getAmount()) + "원(₩), 부가세 별도.",
        normalFont
      ));
      document.add(new Paragraph("\n"));

      // 제3조
      document.add(new Paragraph("제 3 조 (기타 사항)", articleTitleFont));
      document.add(new Paragraph("1. 명시되지 않은 사항은 대한민국 법령 및 일반 상거래 관행을 따른다.", normalFont));
      document.add(new Paragraph("2. 본 계약은 당사자 전자서명 완료 시 효력이 발생한다.", normalFont));
      document.add(new Paragraph("\n\n"));

      // 서명 안내
      Paragraph signHeader = new Paragraph(
        "본 계약을 증명하기 위하여 계약 당사자는 아래와 같이 서명을 포함한다.",
        normalFont
      );
      signHeader.setAlignment(Element.ALIGN_CENTER);
      document.add(signHeader);
      document.add(new Paragraph("\n"));

      Paragraph signDate = new Paragraph(
        "계약 체결일: " + contract.getCreatedAt().toLocalDate(),
        articleTitleFont
      );
      signDate.setAlignment(Element.ALIGN_CENTER);
      document.add(signDate);
      document.add(new Paragraph("\n\n"));

      // 서명 테이블
      PdfPTable signTable = new PdfPTable(2);
      signTable.setWidthPercentage(80);
      signTable.setSpacingBefore(10f);
      signTable.setHorizontalAlignment(Element.ALIGN_CENTER);

      // ==========================
      //   갑(의뢰인) 서명 셀
      // ==========================
      PdfPCell clientSign = new PdfPCell();
      clientSign.setBorder(Rectangle.NO_BORDER);
      clientSign.setPaddingBottom(50f);

      // 역할: 의뢰인 서명
      Paragraph clientRoleLabel = new Paragraph(
        client.getRole().getKorName() + " 서명",
        normalFont
      );
      clientSign.addElement(clientRoleLabel);

      // 실제 서명 이미지 추가
      Image clientSignatureImage = Image.getInstance(contract.getClientSign());
      clientSignatureImage.scaleToFit(200, 100);
      clientSignatureImage.setAlignment(Image.ALIGN_LEFT);
      clientSign.addElement(clientSignatureImage);


      // ==========================
      //   을(프리랜서) 서명 셀
      // ==========================
      PdfPCell freelancerSign = new PdfPCell();
      freelancerSign.setBorder(Rectangle.NO_BORDER);
      freelancerSign.setPaddingBottom(50f);


      // 역할: 프리랜서 서명
      Paragraph freelancerRoleLabel = new Paragraph(
        freelancer.getRole().getKorName() + " 서명",
        normalFont
      );
      freelancerSign.addElement(freelancerRoleLabel);

      // 실제 서명 이미지 추가
      Image freelancerSignatureImage = Image.getInstance(contract.getFreelancerSign());
      freelancerSignatureImage.scaleToFit(200, 100);
      freelancerSignatureImage.setAlignment(Image.ALIGN_RIGHT);
      freelancerSign.addElement(freelancerSignatureImage);


      // ==========================
      //   테이블 추가
      // ==========================
      signTable.addCell(clientSign);
      signTable.addCell(freelancerSign);

      document.add(signTable);


      document.close();
      return baos.toByteArray();
    } finally {
      if (document.isOpen()) {
        document.close();
      }
    }
  }

  // -----------------------------
  // Helper functions
  // -----------------------------

  private PdfPCell createContentCell(String text, int align, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setHorizontalAlignment(align);
    cell.setPadding(5);
    return cell;
  }

  private PdfPCell createHeaderCell(String text, Font headerFont) {
    PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setBackgroundColor(new Color(50, 50, 50));
    cell.setPadding(5);
    return cell;
  }
}
