package ch.epfl.dedis.sicpa;

import ch.epfl.dedis.lib.Roster;

import javax.xml.bind.DatatypeConverter;

public class Rosters{
    public static String LocalRoster = "[[servers]]\n" +
            "  Address = \"tcp://localhost:7002\"\n" +
            "  Suite = \"Ed25519\"\n" +
            "  Public = \"a863cf64422ab15f405369134cd057f99e2b40cb45afe7848dde11f34853f708\"\n" +
            "  Description = \"Conode_1\"\n" +
            "[[servers]]\n" +
            "  Address = \"tcp://localhost:7004\"\n" +
            "  Suite = \"Ed25519\"\n" +
            "  Public = \"4706d99de05a58179ccc11ea3c452d9e44b43290de696f83f0fbc8ae26b6679a\"\n" +
            "  Description = \"Conode_2\"\n" +
            "[[servers]]\n" +
            "  Address = \"tcp://localhost:7006\"\n" +
            "  Suite = \"Ed25519\"\n" +
            "  Public = \"4c4d5dd6fa750d5fb32f005b0a357a39d3886454d9fe63255a89ef0542f835d9\"\n" +
            "  Description = \"Conode_3\"\n";
    public static Roster Local = Roster.FromToml(LocalRoster);
    public static byte[] LocalID = DatatypeConverter.parseHexBinary("ECF578FEBC66E3DDCCA1321D98B997E512DD47E96C254DFEBA6206AB29DFE8E5");
}
