package emissorsat;

import com.sun.jna.Native;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import jpkj.*;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class DLL {

    public static Interface dll = null;
    public static int numeroSessao = 0;

    public static void Load() {
        File local = new File(Bd.get("select valor from config where chave='lib'").get(0).get("valor"));
        if (!local.exists()) {
            Util.showJFrame(new gui.PrimeiraVez(), "Configuração", true);
        }
        Runtime.getRuntime().load(local.getPath());
        DLL.dll = (Interface) Native.loadLibrary(local.getPath(), Interface.class);

    }

    public static String CancelarUltimaVenda() {
        String r = "";
        String cnpjLoja = "";
        String assinatura = "";
        String caixa = "";
        String codigoAtivacao = Bd.get("select valor from config where chave='codigoAtivacao'").get(0).get("valor");
        HashMap<String, String> last;
        try {
            last = Bd.get("select documento,id,numeroSessao,EEEEE,CCCC,mensagem,arquivoCFeBase64,chaveConsulta,valorTotalCFe,CPFCNPJValue,assinaturaQRCODE from retornos where cancelado='n' and momento >= datetime('now','localtime','-30 minutes') order by id desc limit 1").get(0);
        } catch (Exception e) {
            return "Erro Nenhuma Venda Para Cancelar";
        }
        UtilFile.write("cancelando.xml", Util.base64Decode(last.get("arquivoCFeBase64")));
        try {
            SAXBuilder b = new SAXBuilder();
//            Element CFe = b.build(new File("tmp2.xml")).getRootElement();
            Element CFe = b.build(new File("cancelando.xml")).getRootElement();
//            CFe.getChild("");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        xml += "<CFeCanc>";
        xml += "<infCFe chCanc=\"" + last.get("chaveConsulta") + "\">";
        xml += "<ide >";
        xml += "<CNPJ>" + cnpjLoja + "</CNPJ>";
        xml += "<signAC>" + assinatura + "</signAC>";
        xml += "<numeroCaixa>" + caixa + "</numeroCaixa>";
        xml += "</ide>";
        xml += "<emit />";
        if (last.get("CPFCNPJValue").isEmpty()) {
            xml += "<dest />";
        } else {
            xml += "<dest>";
            xml += "<CNPJ>" + last.get("CPFCNPJValue") + "</CNPJ>";
            xml += "</dest>";
        }
        xml += "<total />";
        xml += "<infAdic />";
        xml += "</infCFe>";
        xml += "</CFeCanc>";

        r = DLL.dll.CancelarUltimaVenda(getRandom(6), codigoAtivacao, last.get("chaveConsulta"), xml.getBytes());
        Bd.exec("update retornos set cancelado='s' where id='" + last.get("id") + "'");
        return r;
    }

    public static String TesteFimAFim(String dados) {
        String r = "";
        String codigoAtivacao = Bd.get("select valor from config where chave='codigoAtivacao'").get(0).get("valor");
        try {
            byte[] array = Arrays.copyOf(dados.getBytes("UTF-8"), dados.getBytes("UTF-8").length + 1);
            array[dados.getBytes("UTF-8").length] = 0;
            numeroSessao = getRandom(6);
            r = DLL.dll.TesteFimAFim(numeroSessao, codigoAtivacao, dados.getBytes("UTF-8"));
        } catch (Exception e) {
            Msg.show(Util.exeptionToString(e));
            e.printStackTrace();
        }
        return r;
    }

    public static String ConsultarSAT() {
        String r = "";
        try {
            numeroSessao = getRandom(6);
            r = DLL.dll.ConsultarSAT(numeroSessao);
        } catch (Exception e) {
            Msg.show(Util.exeptionToString(e));
            e.printStackTrace();
        }
        return r;
    }

    public static String EnviarDadosVenda(String dados) {
        String codigoAtivacao = Bd.get("select valor from config where chave='codigoAtivacao'").get(0).get("valor");
        dados = dados.replaceAll("\\n", "");
        String r = "";
        try {
            byte[] array = Arrays.copyOf(dados.getBytes("UTF-8"), dados.getBytes("UTF-8").length + 1);
            array[dados.getBytes("UTF-8").length] = 0;
            numeroSessao = getRandom(6);
            r = DLL.dll.EnviarDadosVenda(numeroSessao, codigoAtivacao, array);
        } catch (Exception e) {
            Msg.show(Util.exeptionToString(e));
            e.printStackTrace();
        }
        return r;
    }

    public static String ConsultarNumeroSessao(int numeroDaSessao) {
        String r = "";
        numeroSessao = getRandom(6);
        String codigoAtivacao = Bd.get("select valor from config where chave='codigoAtivacao'").get(0).get("valor");
        r = DLL.dll.ConsultarNumeroSessao(numeroSessao, codigoAtivacao, numeroDaSessao);
        return r;
    }

    private static int getRandom(int tamanho) {
        int max = 1;
        for (int i = 0; i < tamanho; i++) {
            max *= 10;
        }
        int resultado = new Random().nextInt(max);
        if (resultado >= max / 10) {
            return resultado;
        }
        return getRandom(tamanho);
    }
}
