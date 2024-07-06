
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
(def direction3 (atom :right))
(def direction4 (atom :left))

(def pacman1-x (atom 20))  ; Pacman 1 starts at the second cell of the second row
(def pacman1-y (atom 20))
(def pacman2-x (atom 720)) ; Pacman 2 starts at the second cell of the last row but one
(def pacman2-y (atom 720))

(def ghost1-x (atom 400))  ; ghost 1 empieza en la tercera celda de la segunda fila
(def ghost1-y (atom 400))
(def ghost2-x (atom 400))  ; ghost 2 empieza en la cuarta celda de la segunda fila
(def ghost2-y (atom 400))

(def move-step 20)
(def images (atom {}))


(def map-grid
  [[1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 0 0 1 1 1 0 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 1 1 0 1 1 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 1 1 1 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
   [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]])

;Funciones para cargar las imagenes

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
  (let [key (case @direction
              :up :up2
              :down :down2
              :right :right2
              :left :left2
              :closed)]
    (get @images key)))

;Funciones para las bombas

(def bomb-config {:size 20 :positionx 200 :positiony 200 :visible false :explosion-time nil :max-explosion-size 150 :image nil})


(def ghost-bombs (atom {:ghost1 {:positionx 0 :positiony 0 :visible false :explosion-time nil :size 20 :max-explosion-size 150}
                        :ghost2 {:positionx 0 :positiony 0 :visible false :explosion-time nil :size 20 :max-explosion-size 150}}))

(defn update-ghost-bombs [ghost]
  (let [ghost-pos (if (= ghost :ghost1) {:x @ghost1-x :y @ghost1-y} {:x @ghost2-x :y @ghost2-y})
        bomb (get @ghost-bombs ghost)]
    (when (not (:visible bomb))
      (swap! ghost-bombs assoc-in [ghost :positionx] (:x ghost-pos))
      (swap! ghost-bombs assoc-in [ghost :positiony] (:y ghost-pos))
      (swap! ghost-bombs assoc-in [ghost :visible] true)
      (future
        (Thread/sleep 2000)
        (swap! ghost-bombs assoc-in [ghost :visible] false)
        (swap! ghost-bombs assoc-in [ghost :explosion-time] 10000)))))

(defn schedule-ghost-bombs []
  (future
    (loop []
      (update-ghost-bombs :ghost1)
      (update-ghost-bombs :ghost2)
      (Thread/sleep 3000)
      (recur))))

(def players-bombs (atom {:pacman1 nil :pacman2 nil :blinky nil :pinky nil :inky nil :clyde nil}))

(defn add-bomb-to-player [id]
  (swap! players-bombs assoc id (atom bomb-config)))

(defn get-bombs-feature [player-id feat]
  (let [player-atom (get @players-bombs player-id)]
    (when player-atom
      (feat @player-atom))))

(defn get-ghost-bombs-feature [ghost-id feat]
  (let [ghost-bomb (get @ghost-bombs ghost-id)]
    (when ghost-bomb
      (feat ghost-bomb))))

(defn update-bombs-feature [player-id feat value]
  (when-let [player-atom (get @players-bombs player-id)]
    (swap! player-atom assoc feat value)))



;Colision entre explosiones simplificado
(defn collision-with-explosion [bomb-id pacman-x pacman-y pacman-size bomb-type]
  (let [bomb-feature-fn (if (= bomb-type :player) get-bombs-feature get-ghost-bombs-feature)]
    (when (and (not (nil? (bomb-feature-fn bomb-id :explosion-time)))
               (not (true? (bomb-feature-fn bomb-id :visible))))
      (let [explosion-center-x (bomb-feature-fn bomb-id :positionx)
            explosion-center-y (bomb-feature-fn bomb-id :positiony)
            pacman-center-x (+ pacman-x (/ pacman-size 2))
            pacman-center-y (+ pacman-y (/ pacman-size 2))
            dx (- explosion-center-x pacman-center-x)
            dy (- explosion-center-y pacman-center-y)
            distance (Math/sqrt (+ (* dx dx) (* dy dy)))]
        (< distance (+ (/ (bomb-feature-fn bomb-id :max-explosion-size) 2) (/ pacman-size 2)))))))

(defn collision-with-explosion-enemy1 []
  (collision-with-explosion :pacman1 @pacman2-x @pacman2-y pacman-size :player))

(defn collision-with-explosion-own1 []
  (collision-with-explosion :pacman1 @pacman1-x @pacman1-y pacman-size :player))

(defn collision-with-explosion-enemy2 []
  (collision-with-explosion :pacman2 @pacman1-x @pacman1-y pacman-size :player))

(defn collision-with-explosion-own2 []
  (collision-with-explosion :pacman2 @pacman2-x @pacman2-y pacman-size :player))

(defn collision-with-explosion-ghost1-pacman1 []
  (collision-with-explosion :ghost1 @pacman1-x @pacman1-y pacman-size :ghost))

(defn collision-with-explosion-ghost1-pacman2 []
  (collision-with-explosion :ghost1 @pacman2-x @pacman2-y pacman-size :ghost))

(defn collision-with-explosion-ghost2-pacman1 []
  (collision-with-explosion :ghost2 @pacman1-x @pacman1-y pacman-size :ghost))

(defn collision-with-explosion-ghost2-pacman2 []
  (collision-with-explosion :ghost2 @pacman2-x @pacman2-y pacman-size :ghost))







(defn move-pacman [pacman-x pacman-y direction panel-width panel-height map-grid]
  (let [grid-width (count (first map-grid))  ; Total number of columns
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
      (reset! pacman2-y 720))
    (when (collision-with-explosion-ghost1-pacman1)
      (reset! pacman1-x 20)
      (reset! pacman1-y 20))
    (when (collision-with-explosion-ghost1-pacman2)
      (reset! pacman2-x 720)
      (reset! pacman2-y 720))
    (when (collision-with-explosion-ghost2-pacman1)
      (reset! pacman1-x 20)
      (reset! pacman1-y 20))
    (when (collision-with-explosion-ghost2-pacman2)
      (reset! pacman2-x 720)
      (reset! pacman2-y 720))))


(defn valid-position? [x y grid-width grid-height map-grid]
  (and (>= x 0) (< x grid-width)
       (>= y 0) (< y grid-height)
       (= (get-in map-grid [y x]) 0)))

(defn move-ghost-auto [ghost-x ghost-y direction panel-width panel-height map-grid]
  (let [grid-width (count (first map-grid))    ; Total number of columns
        grid-height (count map-grid)           ; Total number of rows
        move-step 20                           ; Assume a move step of 20 units
        directions [:right :down :left :up]    ; Possible directions
        current-dir-index (atom (.indexOf directions @direction))] ; Current direction index

    (loop [attempts 0]
      (let [next-x (case @direction
                     :left (- @ghost-x move-step)
                     :right (+ @ghost-x move-step)
                     :up @ghost-x
                     :down @ghost-x)
            next-y (case @direction
                     :up (- @ghost-y move-step)
                     :down (+ @ghost-y move-step)
                     :left @ghost-y
                     :right @ghost-y)
            grid-x (int (/ next-x move-step))  ; Adjust this according to your grid setup
            grid-y (int (/ next-y move-step))]  ; Adjust this according to your grid setup

        (if (valid-position? grid-x grid-y grid-width grid-height map-grid)
          (do
            (reset! ghost-x next-x)
            (reset! ghost-y next-y))
          (when (< attempts 4)
            ;; Change direction if the move is invalid
            (swap! current-dir-index #(mod (inc %) (count directions)))
            (reset! direction (nth directions @current-dir-index))
            (recur (inc attempts))))))))





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
            (update-bombs-feature :pacman1 :explosion-time (System/currentTimeMillis))))

        (and (= (.getKeyCode e) KeyEvent/VK_ENTER) (= (get-bombs-feature :pacman2 :visible) false))
        (do
          (update-bombs-feature :pacman2 :positionx @pacman2-x)
          (update-bombs-feature :pacman2 :positiony @pacman2-y)
          (update-bombs-feature :pacman2 :visible true)
          (future
            (Thread/sleep 2000)
            (update-bombs-feature :pacman2 :visible false)
            (update-bombs-feature :pacman2 :explosion-time (System/currentTimeMillis))))))

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

        ;; Dibujar Pac-Mans
        (when pacman-image1
          (draw-image g @pacman1-x @pacman1-y pacman-size pacman-size pacman-image1)
          (when (nil? (get @players-bombs :pacman1))
            (add-bomb-to-player :pacman1)
            (update-bombs-feature :pacman1 :image bomb-image)))

        (when pacman-image2
          (draw-image g @pacman2-x @pacman2-y pacman-size pacman-size pacman-image2)
          (when (nil? (get @players-bombs :pacman2))
            (add-bomb-to-player :pacman2)
            (update-bombs-feature :pacman2 :image bomb-image)))

        ;; Dibujar Fantasmas
        (when ghost-image1
          (draw-image g @ghost1-x @ghost1-y pacman-size pacman-size ghost-image1))
        (when ghost-image2
          (draw-image g @ghost2-x @ghost2-y pacman-size pacman-size ghost-image2))

        ;; Dibujar Bombas de los Pac-Mans
        (doseq [pacman [:pacman1 :pacman2]]
          (when (= (get-bombs-feature pacman :visible) true)
            (draw-image g (get-bombs-feature pacman :positionx) (get-bombs-feature pacman :positiony) (get-bombs-feature pacman :size) (get-bombs-feature pacman :size) (get-bombs-feature pacman :image)))
          (when (and (not (nil? (get-bombs-feature pacman :explosion-time))) (not (= (get-bombs-feature pacman :visible) true)))
            (let [elapsed-time (- (System/currentTimeMillis) (get-bombs-feature pacman :explosion-time))
                  current-explosion-size (min elapsed-time (get-bombs-feature pacman :max-explosion-size))
                  explosion-x (get-bombs-feature pacman :positionx)
                  explosion-y (get-bombs-feature pacman :positiony)]
              (.setColor g (if (= pacman :pacman1) Color/YELLOW Color/RED))
              (.fillOval g (- explosion-x (/ current-explosion-size 2))
                         (- explosion-y (/ current-explosion-size 2))
                         current-explosion-size current-explosion-size)
              (update-bombs-feature pacman :explosion-time nil))))

        ;; Dibujar Bombas de los Fantasmas
        (doseq [ghost [:ghost1 :ghost2]]
          (let [bomb (get @ghost-bombs ghost)]
            (when (:visible bomb)
              (draw-image g (:positionx bomb) (:positiony bomb) (:size bomb) (:size bomb) bomb-image))
            (when (and (not (nil? (:explosion-time bomb))) (not (:visible bomb)))
              (let [elapsed-time (- (System/currentTimeMillis) (:explosion-time bomb))
                    current-explosion-size (min elapsed-time (:max-explosion-size bomb))
                    explosion-x (:positionx bomb)
                    explosion-y (:positiony bomb)]
                (.setColor g (if (= ghost :ghost1) Color/BLUE Color/GREEN))
                (.fillOval g (- explosion-x (/ current-explosion-size 2))
                           (- explosion-y (/ current-explosion-size 2))
                           current-explosion-size current-explosion-size)
                (swap! ghost-bombs assoc-in [ghost :explosion-time] nil)))))

        ;; Verificar Colisiones
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
          (reset! pacman2-y 720))
        (when (collision-with-explosion-ghost1-pacman1)
          (reset! pacman1-x 20)
          (reset! pacman1-y 20))
        (when (collision-with-explosion-ghost1-pacman2)
          (reset! pacman2-x 720)
          (reset! pacman2-y 720))
        (when (collision-with-explosion-ghost2-pacman1)
          (reset! pacman1-x 20)
          (reset! pacman1-y 20))
        (when (collision-with-explosion-ghost2-pacman2)
          (reset! pacman2-x 720)
          (reset! pacman2-y 720))))))


(schedule-ghost-bombs)




(defn create-window []
  (let [frame (JFrame. "Pacman")
        panel (create-pacman-panel)
        timer (Timer. 100 (reify ActionListener
                            (actionPerformed [_ _]
                              (move-ghost-auto ghost2-x ghost2-y direction4 800 800 map-grid)
                              (move-ghost-auto ghost1-x ghost1-y direction3 800 800 map-grid)
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

                  :up2 (load-image "resources/imgs/fantasma1-left.png")
                  :down2 (load-image "resources/imgs/fantasma1-left.png")
                  :left2 (load-image "resources/imgs/fantasma1-left.png")
                  :right2 (load-image "resources/imgs/fantasma1-left.png")
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
