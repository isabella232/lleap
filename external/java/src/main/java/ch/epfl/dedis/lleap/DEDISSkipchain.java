package ch.epfl.dedis.lleap;

import ch.epfl.dedis.lib.CISC;
import ch.epfl.dedis.lib.SkipBlock;
import ch.epfl.dedis.lib.exception.CothorityException;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * DEDISSkipchain represents the pre-configured skipchain on the dedis-servers. It holds the genesis block, that is,
 * the first block of the skipchain. In this block, all necessary information for the skipchain is included. If the
 * block is changed in any way, its hash will change and it will not verify anymore to the skipchain service.
 */
public class DEDISSkipchain {
    private static final Logger logger = LoggerFactory.getLogger(CISC.class);

    /**
     * getGenesis converts the genesis binary data back into a skipblock.
     *
     * @return gensis-skipblock.
     */
    public static SkipBlock getGenesis() {
        try {
            return new SkipBlock(getGenesisBuf());
        } catch (CothorityException e) {
            throw new RuntimeException("Couldn't parse genesis block");
        }
    }

    /**
     * getGenesisBuf returns an array of bytes representing the genesis block.
     *
     * @return byte[] representation of the genesis block.
     */
    public static byte[] getGenesisBuf(){
        return DatatypeConverter.parseHexBinary(genesisHex);
    }

    /**
     * Returns the public key that can be used to verify a key/value pair from the skipchain.
     *
     * @return PublicKey
     */
    public static PublicKey getPublic() {
        try {
            byte[] pKeyHex = new CISC(getGenesis()).getValue("writer");
            logger.info(DatatypeConverter.printHexBinary(pKeyHex));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(pKeyHex);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidProtocolBufferException | CothorityException | InvalidKeySpecException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Hex dump of genesis block used for the lleap-project. This hex dump is created with the `app` tool from
    // github.com/dedis/lleap/app with the following command:
    //  ./app genesis ../conode/public.toml skipchain_id
    // where the skipchain_id is the id returned by:
    //  ./app create ../conode/public.toml
    private static String genesisHex = "08001014181420142a20a591dfb94192921618b55fed77257cdbb1ed4a92b40cdd8813792a64f919cd723210a7f6cdb747f856b4aff5ece35a8824893210e7220552c3e65a3aa6cdc93ad2d133413a0042004aa8078e5a83e32f12556fb51a4971ac6ccf160804122d0a077365727669636512220a20efec3963f605bd7aace47482f755209e876239ae5394975b546727b32c3104d21ab1040a0677726974657212a60430820222300d06092a864886f70d01010105000382020f003082020a0282020100809465d172d4cfbf244a9599e068e3942f40ad619b5ca8b326f13ef29fd7d20bc568838337d945d53c508f29628102efb39765ab5959e789559178a55fa570ceefcc9573cd72d6b7a70f9ad78d09db30f5df1366183c4f94533fd1562fb441b19705e6891be17a628383078fac237ad171e2bff4885865ff635a31fad9b09f55521a362eeaa1489b53de24b0ad4bafdd275608687493089a925e175ffaf81a1f01e278b4e53e5b4940df495a348067906cd052b64be8d09695442c1f5f541bab9aeb7564db54668f28edadfdb21f513032c04927921e90e45f318c9bf776acc584539410ebf06662b76ecbc8a98b2b379b86d6abeee50fe726c06529a8fba84a359662dd089a21701b079b4f6182babd289b3b17ce8b5bdc54fb0a2e3c89bdcdb0669e0fcdd26c2d0a82bddc5732e3e9b90925fdb279487dca47ff1f2cc184a454563d2be26719422599a2039a341826788e72b7899834a75e17bef5481993f909e48715e8f47c6e40f6c0357f1d0833709f4e68909a79a4c5cb703ae5353e78de4f87fad73ed306770b249e38db1f6f5763c7c8321a00dbadd72893a7516067cd0d3fe83989566202de1b58d2b462c72129e461a41ed69ad8ad3937ee70781392b4c5e16ea592288fce0991d6038c182279a7e2c1f5c038c786128d84c79b510103105781047707b4243bc8f185979f7c31e54c14a05b080c2b8e600a0937eb020301000122b0020a10eb78de9a87225f4d8fffad4e2506a16c12520a20a863cf64422ab15f405369134cd057f99e2b40cb45afe7848dde11f34853f708121067e180aa095854eeaf371b8552e1ad521a1a746c733a2f2f6c6c6561702e64656469732e63683a3135303032220012520a204706d99de05a58179ccc11ea3c452d9e44b43290de696f83f0fbc8ae26b6679a1210bd2f2b84685d5de894944cfced1f75af1a1a746c733a2f2f6c6c6561702e64656469732e63683a3135303034220012520a204c4d5dd6fa750d5fb32f005b0a357a39d3886454d9fe63255a89ef0542f835d9121053433ca24b3a51488719e8c02bf449401a1a746c733a2f2f6c6c6561702e64656469732e63683a313530303622001a2088ae5ac5fb227ab8cbe9d913e86eb068902ff642f25c79c065a2c08cfc5728ed52b0020a10eb78de9a87225f4d8fffad4e2506a16c12520a20a863cf64422ab15f405369134cd057f99e2b40cb45afe7848dde11f34853f708121067e180aa095854eeaf371b8552e1ad521a1a746c733a2f2f6c6c6561702e64656469732e63683a3135303032220012520a204706d99de05a58179ccc11ea3c452d9e44b43290de696f83f0fbc8ae26b6679a1210bd2f2b84685d5de894944cfced1f75af1a1a746c733a2f2f6c6c6561702e64656469732e63683a3135303034220012520a204c4d5dd6fa750d5fb32f005b0a357a39d3886454d9fe63255a89ef0542f835d9121053433ca24b3a51488719e8c02bf449401a1a746c733a2f2f6c6c6561702e64656469732e63683a313530303622001a2088ae5ac5fb227ab8cbe9d913e86eb068902ff642f25c79c065a2c08cfc5728ed5a20111b45b06ad06e29a3c96d3706b055ca2d61e12f07384f89006432ea67ce1ba2";
}
