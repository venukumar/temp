package com.vendsy.bartsy.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
/**
 * 
 * @author Seenu Malireddy
 *
 */

public class AsymmetricCipherUtil {
	
	static String xform = "RSA/ECB/PKCS1Padding";
	
	public static String TAG = "AsymmetricCipherTest";
	
	private static String encrypt(byte[] inpBytes, PublicKey key) {
		String encodedText = null;
		try{
			Cipher cipher = Cipher.getInstance(xform);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encodedBytes = cipher.doFinal(inpBytes);
			byte[] conversion = Base64.encode(encodedBytes,	Base64.DEFAULT);
			encodedText = new String(conversion, "UTF-8");
		}
		catch(Exception e){
			Log.e(TAG, "Exception in rsa encrypt::::"+e.getMessage());
		}
		return encodedText;
	}
	
	public static byte[] decrypt(byte[] inpBytes, PrivateKey key) {
		byte[] b = null;
		try{
			Cipher cipher = Cipher.getInstance(xform);
			cipher.init(Cipher.DECRYPT_MODE, key);
			b=cipher.doFinal(inpBytes);
		}
		catch(Exception e){
			Log.e(TAG, "Exception in decrypt::::"+e.getMessage());
		}
		return b;
	}

	
	
	/**
	 * Returns Public Key object from publicKey string
	 * 
	 * @param publicKey - PEM file(Response from the public key sys call)
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPemPublicKey(String publicKey) throws Exception {

	      String publicKeyPEM = publicKey.replace("-----BEGIN PUBLIC KEY-----\n", "");
	      publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");

	      byte[] decoded = Base64.decode(publicKeyPEM, Base64.DEFAULT);
	      X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
	    //  X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey.getBytes());
	      KeyFactory kf = KeyFactory.getInstance("RSA");
	      
	      return kf.generatePublic(spec);
	}
	
	/**
	 * Encrypted string from the argument value
	 * 
	 * @param value
	 * @param publicKey - PEM file(Response from the public key sys call)
	 * @return 
	 */
	public static String getEncryptedString(String value, String publicKey){
		try {
			PublicKey pemPublicKey = getPemPublicKey(publicKey);
			return encrypt(value.getBytes(), pemPublicKey);
			
		} catch (Exception e) {
			Log.e(TAG, "error:: "+e.getMessage());
		}
		return value;
	}
	
	 public static RSAPublicKey readPublicKey(Context context) {
			
	        InputStream inStream;
	        RSAPublicKey pubkey = null;
	        try {
	        	inStream = context.getAssets().open("bartsy_office.crt");
			   
	            CertificateFactory cf = CertificateFactory.getInstance("X.509");
	            X509Certificate cert =(X509Certificate)cf.generateCertificate(inStream);
	            inStream.close();
			
	            // Read the public key from certificate file
	            pubkey = (RSAPublicKey) cert.getPublicKey();
	            System.out.println( "key:"+pubkey);
			  
	        } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (CertificateException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        return pubkey;
	    }
	
	public static PrivateKey getPemPrivateKey(Context context) throws Exception {
	      
		  InputStream in = context.getAssets().open("bartsy_privateKey.pem");
	      int nRead;
	      byte[] data = new byte[1024];
	      
	      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	      
	      while ((nRead = in.read(data, 0, data.length)) != -1) {
	        buffer.write(data, 0, nRead);
	      }

	      buffer.flush();

	      String temp = new String(buffer.toByteArray());
	      String privKeyPEM = temp.replace("-----BEGIN PRIVATE KEY-----\n", "");
	      privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");
	      //System.out.println("Private key\n"+privKeyPEM);

	      byte [] decoded = Base64.decode(privKeyPEM, android.util.Base64.NO_WRAP);

	      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
	      KeyFactory kf = KeyFactory.getInstance("RSA");
	      
	      return kf.generatePrivate(spec);
	}

}
