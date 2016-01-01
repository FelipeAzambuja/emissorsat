
package emissorsat;
import com.sun.jna.Library;
public abstract interface Interface extends Library {
  public abstract String EnviarDadosVenda(int paramInt, String paramString, byte[] paramArrayOfByte);
  
  public abstract String CancelarUltimaVenda(int paramInt, String paramString1, String paramString2, byte[] paramArrayOfByte);
  
  public abstract String ConsultarSAT(int paramInt);
  
  public abstract String TesteFimAFim(int paramInt, String paramString, byte[] paramArrayOfByte);
  
  public abstract String ConsultarNumeroSessao(int paramInt1, String paramString, int paramInt2);
  
  public abstract String DesligarSAT();
  
}
