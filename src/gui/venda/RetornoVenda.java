package gui.venda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import jpkj.Bd;
import jpkj.Util;

/**
 *
 * @author sat
 */
public class RetornoVenda {

    public String numeroSessao, EEEEE, CCCC, mensagem, cod, mensagemSEFAZ, xml, timestamp, chave, total, cfpcnpj, qrcode, documento;
    public Boolean erro;
    
    public String getNumeroSessao() {
        return numeroSessao;
    }

    public String getEEEEE() {
        return EEEEE;
    }

    public String getCCCC() {
        return CCCC;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getCod() {
        return cod;
    }

    public String getMensagemSEFAZ() {
        return mensagemSEFAZ;
    }

    public String getXml() {
        return xml;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getChave() {
        return chave;
    }

    public String getTotal() {
        return total;
    }

    public String getCfpcnpj() {
        return cfpcnpj;
    }

    public String getQrcode() {
        return qrcode;
    }
    private String raw;
    public static RetornoVenda fromDocumento(String documento){
        RetornoVenda r = new RetornoVenda();
        ArrayList<HashMap<String, String>> tmp = Bd.get("select * from retornos where documento=?", new String[]{documento.trim()});
        if(tmp.size() == 0 ){
            return null;
        }
        HashMap<String, String> rs = tmp.get(0);
        r.numeroSessao = rs.get("numeroSessao");
        r.EEEEE = rs.get("EEEEE");
        r.CCCC = rs.get("CCCC");
        r.cod = rs.get("cod");
        r.mensagem = rs.get("mensagem");
        r.mensagemSEFAZ = rs.get("mensagemSEFAZ");
        r.xml = Util.base64Decode(rs.get("arquivoCFeBase64"));
        r.timestamp = rs.get("timeStamp");
        r.chave = rs.get("chaveConsulta");
        r.total = rs.get("valorTotalCFe");
        r.cfpcnpj = rs.get("CPFCNPJValue");
        r.qrcode = rs.get("assinaturaQRCODE");
        r.erro = false;
        return r;
    }
    public RetornoVenda(){}
    public RetornoVenda(String retorno, String documento) {
        this.raw = retorno;
        String[] r = retorno.split(Pattern.quote("|"));
        this.numeroSessao = r[0];
        this.EEEEE = r[1];
        this.CCCC = r[2];
        this.mensagem = r[3];
        if (r.length < 5) {
            this.erro = true;
        } else {
            this.cod = r[4];
            this.mensagemSEFAZ = r[5];
            this.xml = Util.base64Decode(r[6]);
            this.timestamp = r[7];
            this.chave = r[8];
            this.total = r[9];
            this.cfpcnpj = r[10];
            this.qrcode = r[11];
            this.erro = false;
        }
        Bd.exec("BEGIN");
        Bd.exec("insert into retornos(numeroSessao,EEEEE,CCCC,mensagem,cod,mensagemSEFAZ,arquivoCFeBase64,timeStamp,chaveConsulta,valorTotalCFe,CPFCNPJValue,assinaturaQRCODE,documento      ) "
                + "            values(?           ,?    ,?   ,?       ,?  ,?            ,?               ,?        ,?            ,?            ,?           ,?               ,'" + documento + "')", r);
        Bd.exec("COMMIT");
    }

    @Override
    public String toString() {
        return this.raw;
    }
}
