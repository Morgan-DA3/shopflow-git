package ma.shopflow.commande;

public record ResultatPaiement(
        boolean accepte,
        String reference,
        String motifRefus) {

    public static ResultatPaiement accepte(String reference) {
        return new ResultatPaiement(true, reference, null);
    }

    public static ResultatPaiement refuse(String motif) {
        return new ResultatPaiement(false, null, motif);
    }
}