/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emissorsat;


import jpkj.*;

/**
 *
 * @author sat
 */
public class EmissorSAT {



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Util.changeTheme(Util.SISTEMA);
        Bd.connect("jdbc:sqlite:banco.db");
        DLL.Load();
//        System.out.println(DLL.ConsultarSAT());        
        new gui.index().setVisible(true);
    }

}
