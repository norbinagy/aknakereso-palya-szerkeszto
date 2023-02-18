
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.Border;



/**
 * Ez az osztály tárolja azt a mezőt amelyet a felhasználó szerkeszt,
 * és az ezzel kapcsolatos metódusokat.
 * @author Nagy Norbert
 */
public class Palya {
    /** ebben a tömbben tároljuk a tényleges mezőt */
    private int[][] mezo;
    /** a főosztályból készült adattag, a segítségével férünk hozzá get és set metódusokhoz */
    private AknakeresoGUI gui;
    /**
     * Az osztály konstruktora.
     * @param gui Az AknakeresoGUI referenciája
     */
    public Palya(AknakeresoGUI gui) {
        this.gui = gui;
        mezo = new int[4][4];
    }
    /**
     * A mezo tömbe írja az AknakeresoGUI osztályból lekérdezett jButton-okon
     * szereplő értékeket, valamint további számokkal tölti fel a tömböt.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public void mentes() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        int metodusSorszam=1;
        String ertek;
        nullaz();
        for (int i=0; i<mezo.length; i++) 
            for (int j=0; j<mezo[0].length; j++) {
                ertek = (String) gui.getClass().getDeclaredMethod("getTextjButton" + metodusSorszam).invoke(gui, null);
                if (ertek.equals("X")) {
                    mezo[i][j] = -1;
                    
                    if (i!=0 && j!=0 && mezo[i-1][j-1]!=-1)
                        mezo[i-1][j-1]++;
                    
                    if (i!=0 && mezo[i-1][j]!=-1)
                        mezo[i-1][j]++;
                    
                    if (i!=0 && j!=mezo[0].length-1 && mezo[i-1][j+1]!=-1)
                        mezo[i-1][j+1]++;
                    
                    if (j!=0 && mezo[i][j-1]!=-1)
                        mezo[i][j-1]++;
                    
                    if (j!=mezo[0].length-1 && mezo[i][j+1]!=-1)
                        mezo[i][j+1]++;
                    
                    if (i!=mezo.length-1 && j!=0 && mezo[i+1][j-1]!=-1)
                        mezo[i+1][j-1]++;
                    
                    if (i!=mezo.length-1 && mezo[i+1][j]!=-1)
                        mezo[i+1][j]++;
                    
                    if (i!=mezo.length-1 && j!=mezo[0].length-1 && mezo[i+1][j+1]!=-1)
                        mezo[i+1][j+1]++;
                }
                metodusSorszam++;
            }
        
    }
    /**
     * A mezo tömbben szereplő számokat (azokat amelyeket nem a felhasználó adott meg)
     * megjeleníti az AknakeresoGUI jButton-jain.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public void megjelenit() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    int metodusSorszam=1;
        for (int i=0; i<mezo.length; i++)
            for (int j=0; j<mezo[0].length; j++) {
                if (mezo[i][j]==-1) gui.getClass().getDeclaredMethod("setTextjButton" + metodusSorszam, String.class).invoke(gui, "X");
                else if (mezo[i][j]!=0) gui.getClass().getDeclaredMethod("setTextjButton" + metodusSorszam, String.class).invoke(gui, String.valueOf(mezo[i][j]));
                else gui.getClass().getDeclaredMethod("setTextjButton" + metodusSorszam, String.class).invoke(gui, "");
                metodusSorszam++;
            }
    }
    /**
     * A mezo adatbázisba való feltöltésést végző metódus.
     * @param nev ezzel a névvel fog az adatbázisba kerülni a pálya
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public void feltoltes(String nev) throws ClassNotFoundException, SQLException {
        Connection kapcsolat = null;
        Statement lekerdezes = null;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            kapcsolat = DriverManager.getConnection("jdbc:mysql://localhost/aknakereso", "root", "");
            lekerdezes = kapcsolat.createStatement();
            lekerdezes.execute("INSERT INTO palya " + "VALUES ('" + nev + "', '" + toString() + "')");
        }catch(ClassNotFoundException | SQLException e){
            System.out.println(e.getMessage());
        }finally{
            if (lekerdezes!=null)
                lekerdezes.close();
            if (kapcsolat!=null)
                kapcsolat.close();
        }
    }
    /**
     * Az adatbázisból lekérdezi a megadott nevő pályát majd megjeleníti azt a
     * jButton-okon.
     * @param nev amely nevű pályát akarunk betölteni
     * @throws SQLException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public void betoltes(String nev) throws SQLException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Connection kapcsolat = null;
        Statement lekerdezes = null;
        String sqlMezo = "";
        
        try{
            Class.forName("com.mysql.jdbc.Driver");
            kapcsolat = DriverManager.getConnection("jdbc:mysql://localhost/aknakereso", "root", "");
            lekerdezes = kapcsolat.createStatement();
            ResultSet eredmeny = lekerdezes.executeQuery("SELECT mezo FROM palya WHERE nev='" + nev + "'");
            
            while (eredmeny.next()) {
                sqlMezo = eredmeny.getString("mezo");
            }
        }catch(ClassNotFoundException | SQLException e){
            System.out.println(e.getMessage());
        }finally{
            if (lekerdezes!=null)
                lekerdezes.close();
            if (kapcsolat!=null)
                kapcsolat.close();
        }
        int k=0;
        for (int i=0; i<mezo.length; i++)
            for (int j=0; j<mezo[0].length; j++) {
                if (sqlMezo.charAt(k)=='X')
                    mezo[i][j]=-1;
                else
                    mezo[i][j]=Character.getNumericValue(sqlMezo.charAt(k));
                k++;
            }
        megjelenit();
    }
    /**
     * A felhasználó által kijelölt jButton-ok értékét fűzi össze egy String-é.
     * @return egy String amely a teljes mező állását tartalmazza
     */
    @Override
    public String toString() {
        int metodusSorszam=1;
        String m="";
        try {
            for (int i=0; i<mezo.length; i++)
                for (int j=0; j<mezo[0].length; j++) {
                    if (mezo[i][j]==-1 || null == (Border) gui.getClass().getDeclaredMethod("getBorderjButton" + metodusSorszam).invoke(gui, null))
                        m=m+"0";
                    else
                        m=m+mezo[i][j];
                metodusSorszam++;
                }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Palya.class.getName()).log(Level.SEVERE, null, ex);
        }
        return m;
    }
    /**
     * A mezo tömb minden elemét lenullázza.
     */
    private void nullaz() {
        for (int i=0; i<mezo.length; i++)
            for (int j=0; j<mezo[0].length; j++)
                mezo[i][j]=0;
    }
}
