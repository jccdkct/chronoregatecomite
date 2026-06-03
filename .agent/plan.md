# Plan de développement - Chronocoursejc2

## État Actuel et Tâches Réalisées

### Interface et Expérience Utilisateur (UI/UX)
- [x] **Réorganisation des boutons :** Le bouton de gauche gère maintenant "Démarrer" ou "Arrivée", celui de droite est dédié à "Arrêter" (poids respectifs 1.15 / 0.85).
- [x] **Tableau de bord :** Lignes alternées (Blanc / Gris clair) avec bordures de colonnes pour une meilleure lecture.
- [x] **Affichage des rangs :** Suppression des zéros inutiles sur l'écran (1, 2, 3...) tout en les gardant dans l'export texte (001, 002...).
- [x] **Extension du listing :** Le tableau occupe tout l'espace vertical disponible et recouvre l'image de fond du bas.
- [x] **Fonds d'écran :** Intégration de `fond3.png` en pleine largeur en bas de l'écran principal.
- [x] **Dialogue de départ :** Titre raccourci à "Choix du compte à rebours (minutes)", boutons colorés par procédure, et bouton "Quitter" miniature en bas à gauche.

### Fonctions de Saisie
- [x] **Édition du numéro de voile :** Ajout d'une colonne avec une icône crayon personnalisée (`iconeedit.jpg`).
- [x] **Clavier personnalisé :** Création d'un pavé numérique et d'un pavé alphabétique (bouton de bascule ABC/123) pour saisir les numéros de voile.
- [x] **Suppression de la voix :** Retrait complet de la reconnaissance vocale pour privilégier la saisie manuelle rapide.

### Procédures et Chronométrage
- [x] **Procédures nautiques :** Support de 1-0, 2-1-0, 3-2-1-0, 5-4-1-0, 6-4-1-0, 8-4-1-0 et 10-4-1-0.
- [x] **Bips sonores :** Ajout d'un sélecteur en boucle pour 3 types de bips (Original, Sec, Alerte) dans la barre du haut.
- [x] **Synchronisation :** Ordre chronologique dans le listing (Rang 1 en haut) avec scroll automatique vers la fin.

### Gestion du Système et des Fichiers
- [x] **Démarrage forcé :** Luminosité et volume des notifications à 100% dès le lancement de l'application.
- [x] **Raccourcis rapides :** Boutons de cycle pour la luminosité (20/50/100%) et le volume dans la barre du haut.
- [x] **Stockage ordonné :** Sauvegarde dans `Documents/Chronocourse`.
- [x] **Gestion de l'espace :** Bouton "Nettoyer > 1 mois" pour supprimer les anciennes sauvegardes automatiquement.
- [x] **Partage :** Bouton "Partager" pour envoyer le contenu du texte vers d'autres applications.

## En cours / Prochainement
- [ ] Tests intensifs de visibilité en extérieur sur Samsung Galaxy Xcover 5.
- [ ] Finalisation de la documentation utilisateur.
