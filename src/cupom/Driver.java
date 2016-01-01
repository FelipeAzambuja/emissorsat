package cupom;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.regex.Pattern;
import javax.print.PrintService;
import jpkj.Bd;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import jpkj.UtilFile;
import org.apache.pdfbox.pdmodel.PDDocument;

public class Driver {

    public static void main(String[] args) {
        new Driver().print(null);
    }
    String nome;
    private Object rect;

    public Driver() {
        this.nome = "EPSON";
    }

    public static String formaPagamento(String modo) {
        int cod = Integer.parseInt(modo);
        switch (cod) {
            case 1:
                return "Dinheiro";
            case 2:
                return "Cheque";
            case 3:
                return "Cartao de Crédito";
            case 4:
                return "Cartao de Débito";
            case 5:
                return "Credito Loja";
            case 10:
                return "Vale Alimentação";
            case 11:
                return "Vale Refeição";
            case 12:
                return "Vale Presente";
            case 13:
                return "Vale Combustivel";
            case 99:
                return "Outros";
        }
        return "Dinheiro";
    }

    public static float mmToPt(float mm) {
        return ((72.1f * mm) / 25.4f);
    }

    public static String print(String xml) {
        try {
            Bd.connect("jdbc:sqlite:banco.db");
            UtilFile.write("temporario.xml", xml);
            SAXBuilder b = new SAXBuilder();
//            Element CFe = b.build(new File("tmp2.xml")).getRootElement();
            Element CFe = b.build(new File("temporario.xml")).getRootElement();

            //<editor-fold defaultstate="collapsed" desc="Tags XML">            
            Element infCFe = CFe.getChild("infCFe");
            Element ide = infCFe.getChild("ide");
            Element emit = infCFe.getChild("emit");
            Element enderEmit = emit.getChild("enderEmit");
            Element total = infCFe.getChild("total");
            Element dest = infCFe.getChild("dest");
            List<Element> det = infCFe.getChildren("det");
            Element prod = null;
            Element imposto = null;
            Element pgto = infCFe.getChild("pgto");
            List<Element> MP = pgto.getChildren("MP");
            String infCpl = "";
            if (infCFe.getChild("infAdic") != null) {
                infCpl = infCFe.getChild("infAdic").getChildText("infCpl");
            }
            //</editor-fold>

            com.itextpdf.text.Document pdf = new com.itextpdf.text.Document();
            PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(new File("pdf.pdf")));
            float largura = Float.parseFloat(Bd.get("select valor from config where chave='largura'").get(0).get("valor"));
            float altura = Float.parseFloat(Bd.get("select valor from config where chave='altura'").get(0).get("valor"));
            pdf.setPageSize(new Rectangle(mmToPt(largura), mmToPt(altura)));
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);
            writer.setCompressionLevel(1);
//            pdf.setPageSize(PageSize.A4);
            pdf.setMargins(mmToPt(5), mmToPt(1), 0, 0);
//            pdf.setPageCount(1);
            pdf.open();

//            BaseFont.createFont("", nome, true);
            FontFactory.register(new File("agency_fb.ttf").getPath(), "agencyfb");
            Font fontePadrao = FontFactory.getFont("agencyfb", 8, Font.NORMAL);
            fontePadrao = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
            Font fonteCabecalho = FontFactory.getFont("agencyfb", 16, Font.BOLD);
            fonteCabecalho = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontePequena = FontFactory.getFont("agencyfb", 6, Font.NORMAL);
            fontePequena = new Font(Font.FontFamily.HELVETICA, 6, Font.BOLD);

            //<editor-fold defaultstate="collapsed" desc="Cabeçalho">
            Paragraph pdfRazao = new Paragraph(emit.getChildText("xNome"), fonteCabecalho);
            pdfRazao.setAlignment(pdfRazao.ALIGN_CENTER);
            pdf.add(pdfRazao);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Endereço">
            String endereco = enderEmit.getChildText("xLgr");
            endereco += " nº " + enderEmit.getChildText("nro");
            endereco += " " + enderEmit.getChildText("xCpl");
            Paragraph pdfEndereco = new Paragraph(endereco, fontePadrao);
            pdfEndereco.setAlignment(pdfEndereco.ALIGN_CENTER);
            pdf.add(pdfEndereco);
            endereco = " " + enderEmit.getChildText("xBairro");
            endereco += " " + enderEmit.getChildText("xMun");
            endereco += " " + enderEmit.getChildText("CEP");
            pdfEndereco = new Paragraph(endereco, fontePadrao);
            pdfEndereco.setAlignment(pdfEndereco.ALIGN_CENTER);
            pdf.add(pdfEndereco);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="CNPJ">
            Paragraph pdfCNPJ = new Paragraph("CNPJ " + emit.getChildText("CNPJ") + "   IE " + emit.getChildText("IE"), fontePadrao);
            pdfCNPJ.setAlignment(pdfCNPJ.ALIGN_CENTER);
            pdf.add(pdfCNPJ);
            //</editor-fold>

            pdf.add(new Paragraph("----------------------------------------------------------------------", fontePadrao));

            //<editor-fold defaultstate="collapsed" desc="Extrato">
            Paragraph pdfExtrato = new Paragraph("Extrato nº " + ide.getChildText("cNF") + " / " + ide.getChildText("numeroCaixa"), fontePadrao);
            pdfExtrato.setAlignment(pdfExtrato.ALIGN_CENTER);
            pdf.add(pdfExtrato);
            //</editor-fold>

            pdf.add(new Paragraph("----------------------------------------------------------------------", fontePadrao));
            float vFederal = 0;
            float vEstadual = 0;
            float vMunicipal = 0;
            //<editor-fold defaultstate="collapsed" desc="tabela de itens">
            PdfPTable tabela = new PdfPTable(8);
            tabela.setWidthPercentage(100);
            tabela.setWidths(new float[]{
                10f,//cod
                30f,//desc
                7f,//qtd
                8f,//un
                14f,//vl unit
                6f,//st
                8f,//aliq
                14f,//rs
            });
            tabela.addCell(new Paragraph("cod", fontePequena));
            tabela.addCell(new Paragraph("desc", fontePequena));
            tabela.addCell(new Paragraph("qtd", fontePequena));
            tabela.addCell(new Paragraph("un", fontePequena));
            tabela.addCell(new Paragraph("vl unit", fontePequena));
            tabela.addCell(new Paragraph("st", fontePequena));
            tabela.addCell(new Paragraph("aliq", fontePequena));
            tabela.addCell(new Paragraph("R$", fontePequena));
            for (Element item : det) {
                prod = item.getChild("prod");
                imposto = item.getChild("imposto");
                tabela.addCell(new Paragraph(prod.getChildText("cEAN"), fontePequena));
                tabela.addCell(new Paragraph(prod.getChildText("xProd"), fontePadrao));
                tabela.addCell(new Paragraph((Double.parseDouble(prod.getChildText("qCom")) + "").replaceAll(Pattern.quote(".0"), ""), fontePequena));
                tabela.addCell(new Paragraph(prod.getChildText("uCom"), fontePequena));
                tabela.addCell(new Paragraph(prod.getChildText("vUnCom"), fontePequena));
                String st = "";
                if (imposto.getChildText("pICMSST") != null) {
                    st = imposto.getChildText("pICMSST");
                } else {
                    st = "0";
                }
                String icms = "";
                if (imposto.getChildText("pICMS") != null) {
                    icms = imposto.getChildText("pICMS");
                } else {
                    icms = "0";
                }
                tabela.addCell(new Paragraph(st, fontePequena));
                tabela.addCell(new Paragraph(icms, fontePequena));
                tabela.addCell(new Paragraph(prod.getChildText("vItem"), fontePequena));
                String ncm = prod.getChildText("NCM");
                String origem = "";
                for (String cstcheck : new String[]{"00", "10", "20", "30", "40", "41", "50"}) {
                    Element Eicms = imposto.getChild("ICMS");
                    if (Eicms.getChild("ICMS" + cstcheck) != null) {
                        origem = Eicms.getChild("ICMS" + cstcheck).getChildText("Orig");
                    }
                }
                float v = Float.parseFloat(prod.getChildText("vItem"));
                vFederal = cupom.IBPT.calculaFederal(ncm, origem, v);
                vEstadual = cupom.IBPT.calculaEstadual(ncm, origem, v);
                vMunicipal = cupom.IBPT.calculaMunicipal(ncm, origem, v);
            }
            pdf.add(tabela);
            //</editor-fold>

            pdf.add(new Paragraph("TOTAL " + total.getChildText("vCFe")));

            for (Element MeioPagamento : MP) {
                String tmp = formaPagamento(MeioPagamento.getChildText("cMP")) + " " + MeioPagamento.getChildText("vMP");
                pdf.add(new Paragraph(tmp, fontePadrao));
            }

            pdf.add(new Paragraph("Troco " + pgto.getChildText("vTroco"), fontePadrao));
            pdf.add(new Paragraph(IBPT.frase(vFederal, vEstadual, vMunicipal, Float.parseFloat(total.getChildText("vCFe"))), fontePadrao));

            pdf.add(new Paragraph("----------------------------------------------------------------------", fontePadrao));

            pdf.add(new Paragraph(infCpl, fontePequena));
            pdf.add(new Paragraph("Volte Sempre", fontePadrao));

            pdf.add(new Paragraph("----------------------------------------------------------------------", fontePadrao));

            //<editor-fold defaultstate="collapsed" desc="barcode">
            Barcode128 barcode = new Barcode128();
            barcode.setCode(infCFe.getAttribute("Id").getValue().replaceAll("CFe", ""));
            Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), BaseColor.BLACK, BaseColor.BLACK);
            barcodeImage.scalePercent(84, 100);
            pdf.add(barcodeImage);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Gerar QRCODE">
            String qrcodeText = infCFe.getAttribute("Id").getValue().replaceAll("CFe", "");
            qrcodeText += "|" + ide.getChildText("dEmi") + ide.getChildText("hEmi");
            qrcodeText += "|" + total.getChildText("vCFe");
            qrcodeText += "|" + ((dest.getChildText("CPF") == null) ? "" : dest.getChildText("CPF")) + ((dest.getChildText("CNPJ") == null) ? "" : dest.getChildText("CNPJ"));
            qrcodeText += "|" + ide.getChildText("assinaturaQRCODE");
            BarcodeQRCode qrcode = new BarcodeQRCode(qrcodeText, 7, 7, null);
            Image qrcodeImage = qrcode.getImage();
            qrcodeImage.scalePercent(200);
            qrcodeImage.setAlignment(Image.ALIGN_CENTER);
            pdf.add(qrcodeImage);
            //</editor-fold>

            pdf.close();
            PDDocument load = PDDocument.load(new File("pdf.pdf"));
            String destino = Bd.get("select valor from config where chave='impressora'").get(0).get("valor");
            PrinterJob job = PrinterJob.getPrinterJob();
            PrintService impressora = null;
            for (PrintService impr : job.lookupPrintServices()) {
                if (impr.getName().equalsIgnoreCase(destino)) {
                    impressora = impr;
                }
            }
            job.setPrintService(impressora);
            load.silentPrint(job);
//            Desktop.getDesktop().open(new File("pdf.pdf"));
//            Desktop.getDesktop().print(new File("pdf.pdf"));

            return ide.getChildText("cNF");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
