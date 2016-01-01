/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cupom;

import java.util.ArrayList;
import java.util.HashMap;
import jpkj.Bd;

/**
 *
 * @author felipe
 */
public class IBPT {

    public static boolean nacional(String origem) {
        return (origem.equals("0")
                || origem.equals("3")
                || origem.equals("4")
                || origem.equals("5")
                || origem.equals("8"));
    }

    public static float calculaFederal(String ncm, String origem, float total) {
        float ret = 0;
        String campo = (nacional(origem)) ? "nacional" : "federal";// federal = importados
        ArrayList<HashMap<String, String>> retorno = Bd.get("select " + campo + " as valor from ibpt where ncm = '" + ncm + "'");
        if (retorno.size() > 0) {
            float p = (Float.parseFloat(retorno.get(0).get("valor")) / 100);
            ret = total * p;
        } else {
            ret = 0;
        }
        return ret;
    }

    public static float calculaEstadual(String ncm, String origem, float total) {
        float ret = 0;
        String campo = "estadual";
        ArrayList<HashMap<String, String>> retorno = Bd.get("select " + campo + " as valor from ibpt where ncm = '" + ncm + "'");
        if (retorno.size() > 0) {
            float p = (Float.parseFloat(retorno.get(0).get("valor")) / 100);
            ret = total * p;
        } else {
            ret = 0;
        }
        return ret;
    }

    public static float calculaMunicipal(String ncm, String origem, float total) {
        float ret = 0;
        String campo = "municipal";
        ArrayList<HashMap<String, String>> retorno = Bd.get("select " + campo + " as valor from ibpt where ncm = '" + ncm + "'");
        if (retorno.size() > 0) {
            float p = (Float.parseFloat(retorno.get(0).get("valor")) / 100);
            ret = total * p;
        } else {
            ret = 0;
        }
        return ret;
    }

    public static String frase(float vFederal, float vEstadual, float vMunicipal, float vTotal) {
        String ret = "";
        ret = "Valor Aprox dos Tributos R$ " + Math.round(vFederal + vEstadual + vMunicipal) + " ";
        if (vFederal > 0) {
            ret += "Federal R$ " + Math.round(vFederal) + " ( " + Math.round((vFederal / vTotal) * 100) + "% ) ";
        }
        if (vEstadual > 0) {
            ret += "Estadual R$ " + Math.round(vEstadual) + " ( " + Math.round((vEstadual / vTotal) * 100) + "% ) ";
        }
        if (vMunicipal > 0) {
            ret += "Municipal R$ " + Math.round(vMunicipal) + " ( " + Math.round((vMunicipal / vTotal) * 100) + "% ) ";
        }
        return ret;
    }
}
