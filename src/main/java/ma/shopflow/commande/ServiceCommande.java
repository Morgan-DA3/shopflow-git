package ma.shopflow.commande;

import ma.shopflow.panier.LignePanier;
import ma.shopflow.panier.Panier;

public class ServiceCommande {

    private static final System.Logger LOG =
            System.getLogger(ServiceCommande.class.getName());

    private final StockService stock;
    private final PaiementGateway paiement;
    private final CommandeRepository commandes;
    private final NotificationService notifications;

    public ServiceCommande(
            StockService stock,
            PaiementGateway paiement,
            CommandeRepository commandes,
            NotificationService notifications) {

        this.stock = stock;
        this.paiement = paiement;
        this.commandes = commandes;
        this.notifications = notifications;
    }

    public Commande passerCommande(Client client, Panier panier) {

        // C1 : refuser un panier vide
        if (panier.estVide()) {
            throw new IllegalArgumentException("Le panier est vide");
        }

        // C2 : vérifier tout le stock avant de réserver quoi que ce soit
        for (LignePanier ligne : panier.lignes()) {
            if (!stock.estDisponible(
                    ligne.produit().reference(),
                    ligne.quantite())) {

                throw new RuptureDeStockException(
                        ligne.produit().reference());
            }
        }

        // C3 : réserver les produits
        for (LignePanier ligne : panier.lignes()) {
            stock.reserver(
                    ligne.produit().reference(),
                    ligne.quantite());
        }

        // C3 : débiter le client
        ResultatPaiement resultat =
                paiement.debiter(client.id(), panier.total());

        // C5 : en cas de refus, libérer le stock réservé
        if (!resultat.accepte()) {
            for (LignePanier ligne : panier.lignes()) {
                stock.liberer(
                        ligne.produit().reference(),
                        ligne.quantite());
            }

            throw new PaiementRefuseException(resultat.motifRefus());
        }

        // C4 : enregistrer la commande confirmée
        Commande commande = commandes.enregistrer(
                Commande.confirmee(
                        client.id(),
                        panier.lignes(),
                        panier.total(),
                        resultat.reference()));

        // C6 : une panne d'e-mail ne doit pas annuler la commande payée
        try {
            notifications.envoyerConfirmation(client.email(), commande);
        } catch (RuntimeException e) {
            LOG.log(
                    System.Logger.Level.WARNING,
                    "E-mail non envoyé pour la commande {0} : {1}",
                    commande.id(),
                    e.getMessage());
        }

        return commande;
    }
}