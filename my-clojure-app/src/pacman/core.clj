(ns pacman.core
  (:require [clojure.java.io :as io]
            [clojure.set :refer :all]
            [clojure.string :as str])
  (:import (javax.swing JFrame JPanel Timer)
           (java.awt Color Graphics)
           (java.awt.image BufferedImage)
           (java.awt.event ActionListener KeyEvent KeyListener)
           (javax.imageio ImageIO)
           (java.io File)
           (java.lang Thread)))

(def pacman-size 20)
(def open-mouth 270)
(def closed-mouth 360)
(def angle (atom closed-mouth))

(def direction1 (atom :right-open))
(def direction2 (atom :left-open2))
(def direction3 (atom :right-open3))
(def direction4 (atom :right-open4))

(def pacman1-x (atom 20))  ; Pacman 1 starts at the second cell of the second row
(def pacman1-y (atom 20))
(def pacman2-x (atom 720)) ; Pacman 2 starts at the second cell of the last row but one
(def pacman2-y (atom 720))

(def ghost1-x (atom 100))  ; Pacman 3 empieza en la tercera celda de la segunda fila
(def ghost1-y (atom 40))
(def ghost2-x (atom 200))  ; Pacman 4 empieza en la cuarta celda de la segunda fila
(def ghost2-y (atom 50))



(def move-step 8)
(def images (atom {}))

(def map-grid
  [[1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
   [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
   [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]])

(def bomb-config {:size 20 :positionx 200 :positiony 200 :visible false :explosion-time nil :max-explosion-size 150 :image nil})

(def players-bombs (atom {:pacman1 nil :pacman2 nil :blinky nil :pinky nil :inky nil :clyde nil}))

(defn add-bomb-to-player [id]
  (swap! players-bombs assoc id (atom bomb-config)))

(defn load-image [file-path]
  (try
    (let [image (ImageIO/read (File. file-path))]
      (if image
        (println "Image loaded successfully:" file-path)
        (println "Failed to load image, returned nil:" file-path))
      image)
    (catch Exception e
      (println "Exception while loading image:" file-path (.getMessage e))
      nil)))


(defn draw-image [g x y sizex sizey image]
  (.drawImage g image x y sizex sizey nil))


(defn get-current-image [direction]
  (let [key (if (= @angle closed-mouth)
              (if (.endsWith (name @direction) "2") :closed2 :closed)
              @direction)]
    (get @images key))) ; fallback to :closed if key not found


(defn get-current-ghost-image [direction]
  (let [key (case direction
                :up-open2 :up2
                :down-open2 :down2
                :right-open2 :right2
                :left-open2 :left2
                @direction)]
      (get @images key))) ; fallback to :closed if key not found




#_(defn save-image [image file-path format]
  (try
    (ImageIO/write image format (File. file-path))
    (println "Image saved successfully:" file-path)
    true
    (catch Exception e
      (println "Exception while saving image:" file-path (.getMessage e))
      false)))

#_(defn draw-pacman [g x y image]
  (.drawImage g image x y pacman-size pacman-size nil))

(defn get-bombs-feature [player-id feat]
  (let [player-atom (get @players-bombs player-id)]
    (when player-atom
      (feat @player-atom))))

(defn update-bombs-feature [player-id feat value]
  (when-let [player-atom (get @players-bombs player-id)]
    (swap! player-atom assoc feat value)))


;Funciones de colision con bombas 
(defn collision-with-explosion-enemy1 []
  (when (and (not (= (get-bombs-feature :pacman1 :explosion-time) nil)) (not (= (get-bombs-feature :pacman1 :visible) true)))
    (let [explosion-center-x (get-bombs-feature :pacman1 :positionx)
          explosion-center-y (get-bombs-feature :pacman1 :positiony)
          pacman-center-x (+ @pacman2-x (/ pacman-size 2))
          pacman-center-y (+ @pacman2-y (/ pacman-size 2))
          dx (- explosion-center-x pacman-center-x)
          dy (- explosion-center-y pacman-center-y)
          distance (Math/sqrt (+ (* dx dx) (* dy dy)))]
      (< distance (+ (/ (get-bombs-feature :pacman1 :max-explosion-size) 2) (/ pacman-size 2))))))


(defn collision-with-explosion-own1 []
  (when (and (not (= (get-bombs-feature :pacman1 :explosion-time) nil)) (not (= (get-bombs-feature :pacman1 :visible) true)))
    (let [explosion-center-x (get-bombs-feature :pacman1 :positionx)
          explosion-center-y (get-bombs-feature :pacman1 :positiony)
          pacman-center-x (+ @pacman1-x (/ pacman-size 2))
          pacman-center-y (+ @pacman1-y (/ pacman-size 2))
          dx (- explosion-center-x pacman-center-x)
          dy (- explosion-center-y pacman-center-y)
          distance (Math/sqrt (+ (* dx dx) (* dy dy)))]
      (< distance (+ (/ (get-bombs-feature :pacman1 :max-explosion-size) 2) (/ pacman-size 2))))))



(defn collision-with-explosion-enemy2 []
  (when (and (not (= (get-bombs-feature :pacman2 :explosion-time) nil)) (not (= (get-bombs-feature :pacman2 :visible) true)))
    (let [explosion-center-x (get-bombs-feature :pacman2 :positionx)
          explosion-center-y (get-bombs-feature :pacman2 :positiony)
          pacman-center-x (+ @pacman1-x (/ pacman-size 2))
          pacman-center-y (+ @pacman1-y (/ pacman-size 2))
          dx (- explosion-center-x pacman-center-x)
          dy (- explosion-center-y pacman-center-y)
          distance (Math/sqrt (+ (* dx dx) (* dy dy)))]
      (< distance (+ (/ (get-bombs-feature :pacman2 :max-explosion-size) 2) (/ pacman-size 2))))))



(defn collision-with-explosion-own2 []
  (when (and (not (= (get-bombs-feature :pacman2 :explosion-time) nil)) (not (= (get-bombs-feature :pacman2 :visible) true)))
    (let [explosion-center-x (get-bombs-feature :pacman2 :positionx)
          explosion-center-y (get-bombs-feature :pacman2 :positiony)
          pacman-center-x (+ @pacman2-x (/ pacman-size 2))
          pacman-center-y (+ @pacman2-y (/ pacman-size 2))
          dx (- explosion-center-x pacman-center-x)
          dy (- explosion-center-y pacman-center-y)
          distance (Math/sqrt (+ (* dx dx) (* dy dy)))]
      (< distance (+ (/ (get-bombs-feature :pacman2 :max-explosion-size) 2) (/ pacman-size 2))))))







(defn move-pacman [pacman-x pacman-y direction panel-width panel-height map-grid]
  (let [
        grid-width (count (first map-grid))  ; Total number of columns
        grid-height (count map-grid)         ; Total number of rows

        ; Calculate the next position based on the direction
        next-x (case direction
                 :left-open (- @pacman-x move-step)
                 :right-open (+ @pacman-x move-step) 
                 :left-open2 (- @pacman-x move-step)
                 :right-open2 (+ @pacman-x move-step) 
                 :left-open3 (- @pacman-x move-step)
                 :right-open3 (+ @pacman-x move-step)
                 @pacman-x)
        next-y (case direction
                 :up-open (- @pacman-y move-step)
                 :down-open (+ @pacman-y move-step) 
                 :up-open2 (- @pacman-y move-step)
                 :down-open2 (+ @pacman-y move-step) 
                 :up-open3 (- @pacman-y move-step)
                 :down-open3 (+ @pacman-y move-step)
                 @pacman-y)

        ; Convert the pixel coordinates to grid coordinates
        grid-x (int (/ next-x pacman-size))
        grid-y (int (/ next-y pacman-size))]

    ; Check if the next position is within grid bounds and is not a wall
    (when (and (>= grid-x 0) (< grid-x grid-width)
               (>= grid-y 0) (< grid-y grid-height)
               (= (get-in map-grid [grid-y grid-x]) 0))
      (reset! pacman-x next-x)
      (reset! pacman-y next-y))

    (when (collision-with-explosion-enemy1)
      (reset! pacman2-x 720)
      (reset! pacman2-y 720))
    (when (collision-with-explosion-own1)
      (reset! pacman1-x 20)
      (reset! pacman1-y 20))
    (when (collision-with-explosion-enemy2)
      (reset! pacman1-x 20)
      (reset! pacman1-y 20))
    (when (collision-with-explosion-own2)
      (reset! pacman2-x 720)
      (reset! pacman2-y 720))))





(defn move-ghost-auto [ghost-x ghost-y direction panel-width panel-height map-grid]
  (let [grid-width (count (first map-grid))  ; Total number of columns
        grid-height (count map-grid)         ; Total number of rows
        move-step 10                          ; Asume un paso de movimiento de 1 unidad

        ; Define la dirección en la que quieres que se mueva automáticamente
        auto-direction :right  ; Por ejemplo, moviéndose automáticamente hacia la derecha

        ; Calculate the next position based on the auto-direction
        next-x (case auto-direction
                 :left (- @ghost-x move-step)
                 :right (+ @ghost-x move-step)
                 :up @ghost-x  ; Mantener la posición actual si es dirección hacia arriba
                 :down @ghost-x ; Mantener la posición actual si es dirección hacia abajo
                 @ghost-x)  ; Mantener la posición actual por defecto
        next-y (case auto-direction
                 :up (- @ghost-y move-step)
                 :down (+ @ghost-y move-step)
                 :left @ghost-y  ; Mantener la posición actual si es dirección hacia la izquierda
                 :right @ghost-y  ; Mantener la posición actual si es dirección hacia la derecha
                 @ghost-y)  ; Mantener la posición actual por defecto

        ; Convert the pixel coordinates to grid coordinates
        grid-x (int (/ next-x pacman-size))
        grid-y (int (/ next-y pacman-size))]

    ; Check if the next position is within grid bounds and is not a wall
    (when (and (>= grid-x 0) (< grid-x grid-width)
               (>= grid-y 0) (< grid-y grid-height)
               (= (get-in map-grid [grid-y grid-x]) 0))
      (reset! ghost-x next-x)
      (reset! ghost-y next-y))))







(defn draw-map [g]
  (doseq [y (range (count map-grid))
          x (range (count (first map-grid)))]
    (let [cell (get (get map-grid y) x)]
      (when (= cell 1)
        (.setColor g Color/GRAY)
        (.fillRect g (* x pacman-size) (* y pacman-size) pacman-size pacman-size)))))


(defn create-pacman-panel []
  (proxy [JPanel ActionListener KeyListener] []
    (keyPressed [e]
      (cond
        (= (.getKeyCode e) KeyEvent/VK_W) (reset! direction1 :up-open)
        (= (.getKeyCode e) KeyEvent/VK_S) (reset! direction1 :down-open)
        (= (.getKeyCode e) KeyEvent/VK_D) (reset! direction1 :right-open)
        (= (.getKeyCode e) KeyEvent/VK_A) (reset! direction1 :left-open)

        (= (.getKeyCode e) KeyEvent/VK_UP) (reset! direction2 :up-open2)
        (= (.getKeyCode e) KeyEvent/VK_DOWN) (reset! direction2 :down-open2)
        (= (.getKeyCode e) KeyEvent/VK_RIGHT) (reset! direction2 :right-open2)
        (= (.getKeyCode e) KeyEvent/VK_LEFT) (reset! direction2 :left-open2) 
        
        (and (= (.getKeyCode e) KeyEvent/VK_SPACE) (= (get-bombs-feature :pacman1 :visible) false))
        (do
          (update-bombs-feature :pacman1 :positionx @pacman1-x)
          (update-bombs-feature :pacman1 :positiony @pacman1-y)
          (update-bombs-feature :pacman1 :visible true)
          (future
            (Thread/sleep 2000)
            (update-bombs-feature :pacman1 :visible false)
            (update-bombs-feature :pacman1 :explosion-time 10000)))
        (and (= (.getKeyCode e) KeyEvent/VK_ENTER) (= (get-bombs-feature :pacman2 :visible) false))
        (do
          (update-bombs-feature :pacman2 :positionx @pacman2-x)
          (update-bombs-feature :pacman2 :positiony @pacman2-y)
          (update-bombs-feature :pacman2 :visible true)
          (future
            (Thread/sleep 2000)
            (update-bombs-feature :pacman2 :visible false)
            (update-bombs-feature :pacman2 :explosion-time 10000)))))
    (keyReleased [e])
    (keyTyped [e])
    (paintComponent [g]
      (proxy-super paintComponent g)
      (draw-map g)
      (let [pacman-image1 (get-current-image direction1)
            pacman-image2 (get-current-image direction2)
            ghost-image1 (get-current-ghost-image direction3)
            ghost-image2 (get-current-ghost-image direction4)
            bomb-image (get @images :bomb)]
        
        (when pacman-image1
          (draw-image g @pacman1-x @pacman1-y pacman-size pacman-size pacman-image1)
          (let [bomb-exist (get @players-bombs :pacman1)]
            (when (= bomb-exist nil)
              (add-bomb-to-player :pacman1)
              (update-bombs-feature :pacman1 :image bomb-image))))
        
        (when pacman-image2
          (draw-image g @pacman2-x @pacman2-y pacman-size pacman-size pacman-image2)
          (let [bomb-exist (get @players-bombs :pacman2)]
            (when (= bomb-exist nil)
              (add-bomb-to-player :pacman2)
              (update-bombs-feature :pacman2 :image bomb-image)))) 
        
         (when ghost-image1
          (draw-image g @ghost1-x @ghost1-y pacman-size pacman-size ghost-image1))
        
         (when ghost-image2
          (draw-image g @ghost2-x @ghost2-y pacman-size pacman-size ghost-image2))
           


        (when (= (get-bombs-feature :pacman1 :visible) true)
          (draw-image g (get-bombs-feature :pacman1 :positionx) (get-bombs-feature :pacman1 :positiony) (get-bombs-feature :pacman1 :size) (get-bombs-feature :pacman1 :size) (get-bombs-feature :pacman1 :image)))
        (when (= (get-bombs-feature :pacman2 :visible) true)
          (draw-image g (get-bombs-feature :pacman2 :positionx) (get-bombs-feature :pacman2 :positiony) (get-bombs-feature :pacman2 :size) (get-bombs-feature :pacman2 :size) (get-bombs-feature :pacman2 :image)))
        (when (and (not (= (get-bombs-feature :pacman1 :explosion-time) nil)) (not (= (get-bombs-feature :pacman1 :visible) true)))
          (let [elapsed-time (- (System/currentTimeMillis) (get-bombs-feature :pacman1 :explosion-time))
                current-explosion-size (min elapsed-time (get-bombs-feature :pacman1 :max-explosion-size))
                explosion-x (get-bombs-feature :pacman1 :positionx)
                explosion-y (get-bombs-feature :pacman1 :positiony)]
            (.setColor g Color/YELLOW)
            (.fillOval g (- explosion-x (/ current-explosion-size 2))
                       (- explosion-y (/ current-explosion-size 2))
                       current-explosion-size current-explosion-size)
            (update-bombs-feature :pacman1 :explosion-time nil)))
        (when (and (not (= (get-bombs-feature :pacman2 :explosion-time) nil)) (not (= (get-bombs-feature :pacman2 :visible) true)))
          (let [elapsed-time (- (System/currentTimeMillis) (get-bombs-feature :pacman2 :explosion-time))
                current-explosion-size (min elapsed-time (get-bombs-feature :pacman2 :max-explosion-size))
                explosion-x (get-bombs-feature :pacman2 :positionx)
                explosion-y (get-bombs-feature :pacman2 :positiony)]
            (.setColor g Color/RED)
            (.fillOval g (- explosion-x (/ current-explosion-size 2))
                       (- explosion-y (/ current-explosion-size 2))
                       current-explosion-size current-explosion-size)
            (update-bombs-feature :pacman2 :explosion-time nil)))
        


        (when (collision-with-explosion-enemy1)
          (reset! pacman2-x 720)
          (reset! pacman2-y 720))
        (when (collision-with-explosion-own1)
          (reset! pacman1-x 20)
          (reset! pacman1-y 20))
        (when (collision-with-explosion-enemy2)
          (reset! pacman1-x 20)
          (reset! pacman1-y 20))
        (when (collision-with-explosion-own2)
          (reset! pacman2-x 720)
          (reset! pacman2-y 720))))))






(defn create-window []
  (let [frame (JFrame. "Pacman")
        panel (create-pacman-panel)
        timer (Timer. 100 (reify ActionListener
                            (actionPerformed [_ _] 
                              (move-ghost-auto ghost2-x ghost2-y @direction4 800 800 map-grid)                              
                              (move-ghost-auto ghost1-x ghost1-y @direction3 800 800 map-grid)                              
                              (move-pacman pacman2-x pacman2-y @direction2 800 800 map-grid)
                              (move-pacman pacman1-x pacman1-y @direction1 800 800 map-grid)
                              (swap! angle #(if (= % closed-mouth) open-mouth closed-mouth))
                              (.repaint panel))))]
    (doto frame
      (.setSize 800 800)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.add panel)
      (.setVisible true))
    (.setBackground panel Color/BLACK)
    (.start timer)
    (.addKeyListener panel panel)
    (.requestFocus panel)
    frame))

(defn load-images []
  (reset! images {:closed (load-image "resources/imgs/pacman-closed.png")
                  :up-open (load-image "resources/imgs/pacman-up-open.png")
                  :down-open (load-image "resources/imgs/pacman-down-open.png")
                  :left-open (load-image "resources/imgs/pacman-left-open.png")
                  :right-open (load-image "resources/imgs/pacman-right-open.png")
                  :closed2 (load-image "resources/imgs/pacman2-closed.png")
                  :up-open2 (load-image "resources/imgs/pacman2-up-open.png")
                  :down-open2 (load-image "resources/imgs/pacman2-down-open.png")
                  :left-open2 (load-image "resources/imgs/pacman2-left-open.png")
                  :right-open2 (load-image "resources/imgs/pacman2-right-open.png") 

                  :up-open3 (load-image "resources/imgs/fantasma1-left.png")
                  :down-open3 (load-image "resources/imgs/fantasma1-left.png")
                  :left-open3 (load-image "resources/imgs/fantasma1-left.png")
                  :right-open3 (load-image "resources/imgs/fantasma1-left.png")
                  :bomb (load-image "resources/imgs/Bomba.png")})
  (println "Images loaded:" @images)
  (doseq [[key image] @images]
    (if image
      (println (str "Image for key " key " is loaded successfully."))
      (println (str "Image for key " key " is not loaded.")))))

(defn -main [& args]
  (println "Current working directory:" (System/getProperty "user.dir"))
  (load-images)
  (create-window))