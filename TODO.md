# Merjane – Kata de refactoring (entraînement)

La liste des éléments à prendre en considération et à faire par la suite
## Code

- Les opérations de décrementation doivent être synchronized et volatile ou Atomic
- Il faut gérer le cas ou LeadTime = 0
- SRP le controlleur fait 6 choses à la fois, si je modifie il y aura un risque de regression
- Penser architecture Hexagonale avec des ports et adapters
- Penser Event Sourcing avec le pattern Command, pour envoyer des commandes, 
sans avoir besoin de les déclencher depuis un endpoint web HTTP


## Comportements legacy figés par les tests de caractérisation (à valider avec le métier)

- SEASONAL : le jour même du début de saison (bornes strictes `isAfter`/`isBefore`), le produit n'est pas vendu → notification de délai
- EXPIRABLE : un produit non expiré mais en rupture déclenche une notification d'**expiration** et remet le stock à 0
- NORMAL : en rupture avec `leadTime = 0`, aucune notification n'est envoyée
- SEASONAL : le dernier jour de saison (`isBefore` strict), le produit n'est pas vendu ; si now + leadTime dépasse la fin, le stock restant est remis à 0
- EXPIRABLE : le jour de la date d'expiration (`isAfter` strict), le produit est considéré comme expiré et le stock remis à 0
