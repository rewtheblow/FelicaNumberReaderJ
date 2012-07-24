package jp.co.isid.felica;
import java.io.File;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * このプログラムのソースは、
 * [亜細亜大学であがたが行う授業に関するサイトです。]から流用したものです。
 * 	http://itasan.mydns.jp/wiki.cgi/ASIA?page=Java%A4%CE%B1%FE%CD%D1%A1%A7Felica%A5%AB%A1%BC%A5%C9%A5%EA%A1%BC%A5%C0#p0
 * 
 * プログラムの実行には、
 * 		[Java Native Access (jna.jar)] 
 * 			< https://jna.dev.java.net/ >
 * と、
 * 		[felicalib (felicalib.dll)] 
 * 			< http://felicalib.tmurakam.org/ >
 *  が、別途必要になります。
 * 
 * 
 * @author hayashi
 *
 */
public class Felica {
    final public static short WILDCARD = (short)0xffff;        // ワイルドカード
    final public static short SUICA = 0x03;
    
    /**
     * 'felicalib.dll'へアクセスするためのインターフェイス定義
     * 
     * [felicalib (felicalib.dll)]
     * 		< http://felicalib.tmurakam.org/ >
     *
     */
    public interface FelicaLib extends Library {
        FelicaLib INSTANCE = (FelicaLib) Native.loadLibrary("felicalib", FelicaLib.class);

        Pointer pasori_open(String dummy);
        int pasori_init(Pointer pasoriHandle);
        void pasori_close(Pointer pasoriHandle);
        Pointer felica_polling(Pointer pasoriHandle, int systemCode, byte rfu, byte time_slot);
        void felica_free(Pointer felicaHandle);
        void felica_getidm(Pointer felicaHandle, byte[] data);
        void felica_getpmm(Pointer felicaHandle, byte[] data);
        int felica_read_without_encryption02(Pointer felicaHandle, int serviceCode, int mode, byte addr, byte[] data);
    }
    
    Pointer pasoriHandle;
    Pointer felicaHandle;
    
    /**
     * 'felicalib.dll'操作上のエラー定義
     * @author hayashi
     *
     */
    @SuppressWarnings("serial")
    public class FelicaException extends Exception {
        public FelicaException(String string) {
            super(string);
        }
    }

    /**
     * コンストラクタ。ここでFelicaカードリーダへのハンドルを取得している
     * @throws FelicaException
     */
    public Felica() throws FelicaException {
        pasoriHandle = FelicaLib.INSTANCE.pasori_open(null);
        if (pasoriHandle == null) {
            throw new FelicaException("felicalib.dllを開けません");
        }
        if (FelicaLib.INSTANCE.pasori_init(pasoriHandle) != 0) {
            throw new FelicaException("PaSoRiに接続できません");
        }
    }
    
    /**
     * PaSoRi ハンドルをクローズする 
     * Felicaカードリーダに関する処理を終了する際に呼ぶメソッド
     * 		【　void pasori_close(pasori *p)　】
     */
    public void close() {
        if (felicaHandle != Pointer.NULL) {
            FelicaLib.INSTANCE.felica_free(felicaHandle);
        }
        if (pasoriHandle != Pointer.NULL) {
            FelicaLib.INSTANCE.pasori_close(pasoriHandle);
        }
    }
    
    /**
     * FeliCa をポーリングする
     * 		【　felica*　felica_polling(pasori *p, uint16 systemcode, uint8 RFU, uint8 timeslot)　】
     * 
     * @param systemCode
     * @throws FelicaException
     */
    public void polling(short systemCode) throws FelicaException {
        FelicaLib.INSTANCE.felica_free(felicaHandle);
        felicaHandle = FelicaLib.INSTANCE.felica_polling(pasoriHandle, systemCode, (byte)0, (byte)0);
        if (felicaHandle == Pointer.NULL) {
            throw new FelicaException("カード読み取り失敗");
        }
    }
    
    /**
     * IDm 取得
     * 		【　void felica_getidm(felica *f, uint8 *buf)　】
     * 
     * @return
     * @throws FelicaException
     */
    public byte[] getIDm() throws FelicaException {
        if (felicaHandle == Pointer.NULL) {
            throw new FelicaException("no polling executed.");
        }
        byte[] buf = new byte[8];
        FelicaLib.INSTANCE.felica_getidm(felicaHandle, buf);
        return buf;
    }
    
    /**
     * PMm 取得
     * 		【　void felica_getpmm(felica *f, uint8 *buf)　】
     * 
     * @return
     * @throws FelicaException
     */
    public byte[] getPMm() throws FelicaException {
        if (felicaHandle == Pointer.NULL) {
            throw new FelicaException("no polling executed.");
        }
        byte[] buf = new byte[8];
        FelicaLib.INSTANCE.felica_getpmm(felicaHandle, buf);
        return buf;
    }
    
    /**
     * FelicaカードのID番号を取得するメソッド
     * @param systemCode システムコード(例えばSuicaは0x03、ワイルドカードは0xFF)
     * @return カードのID番号？
     * @throws FelicaException
     */
    public String getID(short systemCode) throws FelicaException {
        FelicaLib.INSTANCE.felica_free(felicaHandle);
        felicaHandle = FelicaLib.INSTANCE.felica_polling(pasoriHandle, systemCode, (byte)0, (byte)0);
        if (felicaHandle == Pointer.NULL) {
            throw new FelicaException("カード読み取り失敗");
        }
        byte[] buf = new byte[8];
        FelicaLib.INSTANCE.felica_getidm(felicaHandle, buf);
        return String.format("%02X%02X%02X%02X%02X%02X%02X%02X", buf[0],buf[1],buf[2],buf[3],buf[4],buf[5],buf[6],buf[7]);
    }

    /**
     * FelicaカードのPMmを取得するメソッド
     * 
     * @param systemCode システムコード(例えばSuicaは0x03、ワイルドカードは0xFF)
     * @return カードのID番号？
     * @throws FelicaException
     */
    public String getPM(short systemCode) throws FelicaException {
        FelicaLib.INSTANCE.felica_free(felicaHandle);
        felicaHandle = FelicaLib.INSTANCE.felica_polling(pasoriHandle, systemCode, (byte)0, (byte)0);
        if (felicaHandle == Pointer.NULL) {
            throw new FelicaException("カード読み取り失敗");
        }
        byte[] buf = new byte[8];
        FelicaLib.INSTANCE.felica_getpmm(felicaHandle, buf);
        return String.format("%02X%02X%02X%02X%02X%02X%02X%02X", buf[0],buf[1],buf[2],buf[3],buf[4],buf[5],buf[6],buf[7]);
    }

	/**
	 * 'PaSoRi'にかざされているFeliCaカードのIDｍ部分を１０回読み出す。
	 * 500ms間隔で１０回読む。
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws FelicaException {
        Felica felica = new Felica();
        for (int i=0;i<10;i++) {
            try {
                System.out.println(i+":"+felica.getID((short)0xFF));
            }
            catch (FelicaException e) {}    //読み取れなかったらそのまま
            
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        felica.close();
	}
}
