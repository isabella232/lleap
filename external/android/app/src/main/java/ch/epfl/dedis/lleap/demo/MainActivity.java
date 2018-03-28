package ch.epfl.dedis.lleap.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;

import ch.epfl.dedis.lleap.KeyValueBlock;
import ch.epfl.dedis.lleap.LLEAPKey;
import ch.epfl.dedis.lleap.SkipchainRPC;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView hw = (TextView) findViewById(R.id.hw);
        String out = "none";

        PrivateKey privateKey = LLEAPKey.getPrivate();
        PublicKey publicKey = LLEAPKey.getPublic();
        RSAPublicKey rsa = (RSAPublicKey)publicKey;
        try {
            SkipchainRPC sc = new SkipchainRPC();
            if (!sc.verify()) {
                out = "couldn't verify";
            } else {

                String keyStr = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
                byte[] key = keyStr.getBytes();
                byte[] value = "hashes".getBytes();
                sc.setKeyValue(key, value, privateKey);

                // Reading it back from the skipchain, and verify the collective signature on the forward link,
                // which implies inclusion
                KeyValueBlock kvb = sc.getKeyValueBlock(key);

                // Verify the signature on the key/value pair
                Signature verify = Signature.getInstance("SHA256withRSA");
                verify.initVerify(publicKey);
                verify.update(kvb.getSignedMsg());

                out = "ok";
            }
        } catch (Throwable e) {
            out = "error: " + e.toString();
        }

        hw.setText(out);
    }
}
