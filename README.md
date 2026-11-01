### Consignes: 
* Ignorez les migrations BDD
* Ne pas modifier les classes qui ont un commentaire: `// WARN: Should not be changed during the exercise
`
* Pour lancer les tests (depuis le sous-répertoire `api`) :
  * unitaires: `mvnw test`
  * integration: `mvnw integration-test`
  * tous: `mvnw verify`

## USE CASES
Règles de gestion du catalogue produit :
Chaque produit est caractérisé par un niveau de stock (available) décrémenté après chaque commande, ainsi que par un délai de réapprovisionnement (leadTime).
- Produits Classiques (NORMAL) : En cas de stock épuisé, le délai de réapprovisionnement est affiché au client.
- Produits Saisonniers (SEASONAL) : En rupture de stock, le réapprovisionnement est proposé uniquement si la date de livraison estimée reste dans la période de la saison. Dans le cas contraire, la commande est bloquée et le client est notifié de l'indisponibilité.
- Produits Périssables (EXPIRABLE) : Le produit est disponible à la vente uniquement jusqu'à sa date de péremption. Une fois la date atteinte, il devient impossible à commander.

## Ce que j'ai fait
- J'ai renommer variables pour la visibilité
- J'ai consacré 10min à la lecture du code
- J'ai fait du TDD, j'ai créer le nom des fonctions des tests, puis leur corps pour le cas NORMAL, puis pour
  SEASONAL et EXPIRABLE  J'ai crée les tests d'intégration qui refletent le code tel quel est avant refactoring,
  Chacune est dans un commit à part.
- Tous les commits sont atomiques, à la fin je vais merger avec rebase la branche refactor/order-workflow dans main
- J'ai injecté Clock en tant que Bean avec valeur Clock.systemDefaultZone(); je vais dorénavant Remplace chaque LocalDate.now() par LocalDate.now(clock)
- Je ne dois pas injecter Clock dans les tests, je ne dois pas modifier le comportement attendu des tests
- Je dois injecter par constructeur pour Order Service car les dépendences sont obligatires
- j'ai crée un enum type ProductType { NORMAL, SEASONAL, EXPIRABLE }
- Design Pattern stratégy au lieu de if else, NormalProductHandler, SeasonalProductHandler et ExpirableProductHandler et mappés dans la boucle for