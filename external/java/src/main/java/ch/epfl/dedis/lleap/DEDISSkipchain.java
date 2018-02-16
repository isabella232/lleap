package ch.epfl.dedis.lleap;

import ch.epfl.dedis.lib.Roster;
import sun.security.rsa.RSAPrivateCrtKeyImpl;

import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * DEDISSkipchain represents the pre-configured skipchain on the dedis-servers. It holds the roster-definition,
 * the skipchain-id and the private/public keypair allowed to write to the skipchain.
 */
public class DEDISSkipchain {
    private static String rosterStr = "[[servers]]\n" +
            "  Address = \"tcp://lleap.dedis.ch:15002\"\n" +
            "  Suite = \"Ed25519\"\n" +
            "  Public = \"23450be83c652c2d88309cced350360b989be08b93ed3ef03f33f90fa62204ab\"\n" +
            "  Description = \"Conode_1\"\n" +
            "[[servers]]\n" +
            "  Address = \"tcp://lleap.dedis.ch:15004\"\n" +
            "  Suite = \"Ed25519\"\n" +
            "  Public = \"b6aee135eb2c562a39fd37fbe270dcdb7da944d4599375fd8242f6bf9d9667ec\"\n" +
            "  Description = \"Conode_2\"\n" +
            "[[servers]]\n" +
            "  Address = \"tcp://lleap.dedis.ch:15006\"\n" +
            "  Suite = \"Ed25519\"\n" +
            "  Public = \"21c83de7c829455a656afca02963488b87704b3d0ed0c309bdc62c79f89e0ae1\"\n" +
            "  Description = \"Conode_3\"\n";

    // roster is used to communicate with the cothority
    public static Roster roster = Roster.FromToml(rosterStr);
    // skipchainID is the pre-configured skipchain on the cothority. As it is a cryptographic hash,
    // it is secure to trust it as nobody should be able to forge a block with the same ID.
    public static byte[] skipchainID = DatatypeConverter.parseHexBinary("77d81678c3b27595d75fed7edf079b1f8804da7d6ecb95cb7b6b7d83954d3abd");

    /**
     * Returns the private key that can be used to sign key/value pairs when they are stored on the
     * skipchain.
     *
     * @return PrivateKey
     */
    public static PrivateKey getPrivate() {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(DatatypeConverter.parseHexBinary(DEDISSkipchain.privateKeyStr)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns the public key that can be used to verify a key/value pair from the skipchain.
     *
     * @return PublicKey
     */
    public static PublicKey getPublic() {
        try {
            RSAPrivateCrtKeyImpl rsaPrivateKey = (RSAPrivateCrtKeyImpl) getPrivate();
            return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Created private key by the TestSkipchainRPC.setupKP method.
    private static String privateKeyStr = "30820941020100300D06092A864886F70D01010105000482092B308209270201000282020100" +
            "883402A12A11E5C56225FDC2055DBE7A2893729F4F40D83CF192CACBBB0A891C559FE309094B0AFCEDBD53461D600E035287285300" +
            "E5F4448C52B7A71F0EE0E596E502E9BB8F1B58376D9B6D68047D01F4499263CA494661E80A7EF690BE1E4AF3C39CC1FD1CBB13A036" +
            "38ED64EFC9BD8E37A08AA40309C37D286A38034486E0EE88AA2C84B41CBA6BB2BA41E5B95F1BFFC63D2DAA9DB1D89EAE721D1CB58D" +
            "04000F70FBAAF6FFE2E3E1808AF8B6FB636836875E9328369B18A195D452C53B4FF8970523898634D351BFFBADDFEC560871F693AE" +
            "3B6E73A12A2F18A7203AE6C46B4D1550FADF06A0F62A616CE66DC698ED4204F3A4E5347CBDF91F381424E319FBC254038814315DC3" +
            "6072B19FDE3481B0D490F2E4854F88976E447A206084DBF280D6519E9B35398D66EFCC6804CF929615232498163A8840B7604C17C3" +
            "A1B11941004DDB0870D621FF63D769D461406F85536AF5F958010DB7382174A0BB5D50FD2033BD3F964093CC337F7416CCE3254547" +
            "35FE09CEB8EC9D3037F492E7187BF5F4834EAB7F55AEED214D46E5E9F54099FEEDE61E2BFA75652FF9598C05AD3C5B9B819872DB22" +
            "888434015612635372E2609F494E5CB7CD85B4F9162C4B6C51CCF1464A7D2C0BFD96C66A0A6FCA608E848934EB28C048BC54D29398" +
            "6CD3B2CF9C9FD6A636AAA065E57975F589FDBEEC6AC18B78EAB45F48B5C47E53C40B1D020301000102820200706C75CB2F9D8C4A0B" +
            "848E5BAAF040FC3627D6D1D6AA1092E557A41ADBED8B551EFC157B6CF74B3F90C0D84EA48A97A67865658C75CE597C8752F3927799" +
            "615B40DD11357A825A30CBB7A6367D484F22E0D0258C46F98AA5DFD1621105525850C7BAABD697A53269B5B16BF5CF7DF9C883EE38" +
            "8E976886939CE8521C9FDDDD73269119DE1B3F4C0338485363774A364E37261B73694436FB35A78A3BAB69D3D6FC71C2718921CDFF" +
            "2AD804B13130607A2217F84EC12ACD3FE1E9E671564CC473731EBCB82398EA5B8F3BE77312C4D212B076B6597766E8FC9B843A5620" +
            "CD5D93BD79E7E16AE1D267E4290137D14FC5408F7E11F6462286DB7C37786462843C0B0D7DF56CF8CD7426C6005F2A19743F5645BB" +
            "752108AFC5B18E84A8868ECF3A5AFE15FCD7F89287DDE184DA9EC77C7A1021410C870FA1D944A992B4BB3CF298CB6EE2CE9CF12C31" +
            "823CAB456A2E320558FB22975448D1518A1A24EBA35827A3CB8FD048AAAEC3B41326D040E349BB492F6CCE01BEF9F3F30EA7F53289" +
            "CEB38A829F32751CD01987DB9EC65A64247C521711537581BF56FE02F160B56DB301474974D20E3B1FF712B89F200ECC1F867E6139" +
            "00692CB0FC4A48485544BC331A51B0B4575A86C3FC5B99D6759A59D935169C2677ACF20A80168E746A1A7F873AA930D26E7E82EF99" +
            "0C924F42EC7C67ACA04B7DD2F16946E6378649A8A8DD5C138CA10282010100CF8EBA3537A8B40DAF472A766F0B2162CD572663E37C" +
            "AAD71C7BA591861EB8745AF681F740C430809231C7E27AD4CFE8F5CCCBE787642D66971AB794C1EA4C09206D6F97A6F0313AAB856E" +
            "3795047E266B47405C1B776F8802627E28C04C7B555CDF5EB6E55BC89533699E1ABB165F1EC5B0D64A2C95A002A93832D7927FF2EF" +
            "8DDCE7C5CE45B151232392311F4C773214E4FEFED8CE2EEE10E1B711DF973737159817578CE828B90663B00941F8B12C4437BABEC9" +
            "380310D4EA1E28065DDE98F2634A0CD2755A853BA3AE05DBEB85CB71D31FEC1CE3F03D72E1771C88AC2182D87BD48875F3077B1018" +
            "3A4F9EB019D9503373D29103658A709BC2305AA832250282010100A7FDF5928FC4170A1D1F6EFA5E2EDD98369671212E042B5DE66B" +
            "7C859818673AB3F1C42810521191B34DF8D798ECF61FB8D696B7F852F1C55F2A2216B323CCE4D8B74222CA2EC0AAEC39D824DC8053" +
            "F230E64A69E65858A9864D7B6E6F05E077ECA3C6898D73ADD5CD9A43085CA68E4DC63BBD243AE0D1DAB8C25327FEC81600282EE237" +
            "DCA3E9C5CCF9FAF599AC0DAD18C65E55C511922BFE6D74098FDA6FA3AE7565E64B7EA51B251CE7B08D1FCE4FF0D6DDE74E0724C8A1" +
            "3FDFE5A9A79B27E9947CA7E5B8FCE37AC3344225F4BE7BCEB7C3D388CD12A541952BB9E9B53B6E0ACF085183D0A860D1537E071E67" +
            "19D4E9E7F1D28DD91BD2B4483E02364DD7990282010043599B0EBDBADE823A482B7D36D733C42DC183D191F5D831E92A2E35A481BF" +
            "74F2375F40EE213B63DD0C8E41ED7DB4E171313D5129DBD5E79E4CAB19783B20E52D42959D6E2C2EAD4D0F050EC02A2F0D246E8071" +
            "E2EFC49F2BB6EE1D27192A442ED8C49130A7B1D4C854135BB52DB33BF70644E2D8C8CC9D506FCDFFA9A81F1AA0BBF7F175D7A38C22" +
            "8CDE595640A7C4F686C03FCDF649C032762BC37EFC408C7D5356EFAFE77D9F5C75689FF86A4C8382B75ABFA1E7AD06FF2FE10D37D3" +
            "A9F19365A897B48B240E6BA5D7484B6E8781D6B39D7E2E79C07110201D5882F8E7FEDF647F38B09D65E606F8F5A2F4694C59F9EE58" +
            "3D11953A93CBAA9F3EB58C328902820100154C70771D7D1A02B1408BFC78366EE7DE7BE269F78095D2F8E4C5CBD645B9C8CA89AB79" +
            "FCDFBAD4832C2E917F1503FB6F9EDD4B03D3D84D52CC6FB7FB0C5DECE1C1124480E7BE8BAD19845AE00DF116B2B66FD6CF5A821B34" +
            "28DCAE5AAB1F1375502A5AC4315767D5026ADF4387E660DBF234FFC3EB3F1000FEBA3646EA2D47E5D053E40B3848EA481BA582BFF1" +
            "FD65E5CF38A49D30A61CA7CC4EBAEB9F212A5A730DF14DBF07C5D245C0E6346E44E503BEEA316A462396C042EDEB1B4061BE84B1CB" +
            "C029B2F3750FDA71E4B5153B954CD7259FCB275CE6D0D2CDD16B5D0BE4DEBFF2E90AD24896C5D3551627F7A061CB2A1A11BCE43ACF" +
            "D30599BF040D88790282010035A0ADFC8FD9AB09630AB7D85EDD0E01D9BF2FC63860227F4AAB19E6DDF4E12CB36486470D4CF6AFE0" +
            "B71BA4CC894DA22B505EE6E1EC3750C998EE9EA233B59E47D7DBE5E90250EE143E162ADF759746E093CDE22BD1A84DB40666C80BF5" +
            "D102D24A1A33E09A9A00A66C07F173F5372D20A2D08DC03EE28B4405CFE8D9D1D2C8EB2053EFCEFB65B4197CB37F6C5A407D90C3EE" +
            "773D0A2284116A6C617FEFC330E6BA142D8F5FA78F21BBD05A5724B739A23BA70081F83B1FEA08CB5E68135F82676EB4B394A33F3C" +
            "615F8ED4CD578AA43F7F0AE48545EBFBAB706FD058AA2B3E746185E563731BB4287267C6B6F52A588D3FB708C3B45BEDEECA3B791C" +
            "0E38D5";
}