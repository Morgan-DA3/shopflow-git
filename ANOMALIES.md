# ANOMALIES — TP1 et TP2

## TP2 — Service de commande

### Anomalie 1 : stock non libéré après un paiement refusé

**Problème :** lorsqu'un paiement était refusé, le service levait `PaiementRefuseException` sans libérer les produits précédemment réservés.

**Conséquence :** le stock pouvait rester bloqué alors que la commande n'était pas payée.

**Détection :** le test `unPaiementRefuseLibereLeStock()` utilise Mockito pour vérifier l'appel à `stock.liberer()`.

**Correction :** libérer chaque ligne du panier réservé avant de lever l'exception.

### Anomalie 2 : une panne d'e-mail faisait échouer une commande payée

**Problème :** une exception lors de l'envoi de l'e-mail remontait jusqu'au client, alors que le paiement avait déjà été accepté et la commande enregistrée.

**Conséquence :** le client pouvait croire que sa commande avait échoué et essayer de la repasser.

**Détection :** le test `unePanneDeMailNAnnulePasLaCommande()` simule une panne du serveur SMTP avec Mockito.

**Correction :** intercepter l'exception d'exécution lors de l'envoi, journaliser l'erreur et retourner la commande confirmée.
