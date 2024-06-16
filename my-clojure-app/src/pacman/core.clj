(ns pacman.core
  (:require [clojure.java.io :as io]
            [clojure.set :refer :all])
  (:import (javax.swing JFrame JPanel Timer)
           (java.awt Color)
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

(def pacman1-x (atom 0)) ; Coordenada x inicial del Pacman 1
(def pacman1-y (atom 10)) ; Coordenada y inicial del Pacman 1

(def pacman2-x (atom 700)) ; Coordenada x inicial del Pacman 2
(def pacman2-y (atom 730)) ; Coordenada y inicial del Pacman 2

(def move-step 8) ; Cantidad de píxeles que Pacman se mueve en cada paso

(def images (atom {}))

;; Caracteriristicas de la bomba
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

(defn get-bombs-feature [player-id feat]
  (let [player-atom (get @players-bombs player-id)]
    (when player-atom
      (feat @player-atom))))

(defn update-bombs-feature [player-id feat value]
  (when-let [player-atom (get @players-bombs player-id)]
    (swap! player-atom assoc feat value)))

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

(defn move-pacman [pacman-x pacman-y direction panel-width panel-height]
  (cond
    (= @direction :up-open) (swap! pacman-y #(max 0 (- % move-step)))
    (= @direction :down-open) (swap! pacman-y #(min (- panel-height pacman-size) (+ % move-step)))
    (= @direction :left-open) (swap! pacman-x #(max 0 (- % move-step)))
    (= @direction :right-open) (swap! pacman-x #(min (- panel-width pacman-size) (+ % move-step)))
    (= @direction :up-open2) (swap! pacman-y #(max 0 (- % move-step)))
    (= @direction :down-open2) (swap! pacman-y #(min (- panel-height pacman-size) (+ % move-step)))
    (= @direction :left-open2) (swap! pacman-x #(max 0 (- % move-step)))
    (= @direction :right-open2) (swap! pacman-x #(min (- panel-width pacman-size) (+ % move-step))))
  (when (collision-with-explosion-enemy1)
    (reset! pacman2-x 700)
    (reset! pacman2-y 730))
  (when (collision-with-explosion-own1)
    (reset! pacman1-x 0)
    (reset! pacman1-y 0))
  (when (collision-with-explosion-enemy2)
    (reset! pacman1-x 0)
    (reset! pacman1-y 0))
  (when (collision-with-explosion-own2)
    (reset! pacman2-x 700) 
    (reset! pacman2-y 730)))

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
      (let [pacman-image1 (get-current-image direction1)
            pacman-image2 (get-current-image direction2)
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
          (reset! pacman2-x 700)
          (reset! pacman2-y 730))
        (when (collision-with-explosion-own1)
          (reset! pacman1-x 0)
          (reset! pacman1-y 0))
        (when (collision-with-explosion-enemy2)
          (reset! pacman1-x 0)
          (reset! pacman1-y 0))
        (when (collision-with-explosion-own2)
          (reset! pacman2-x 700)
          (reset! pacman2-y 730))))))

(defn create-window []
  (let [frame (JFrame. "Pacman")
        panel (create-pacman-panel)
        timer (Timer. 100 (reify ActionListener
                            (actionPerformed [_ _]
                              (move-pacman pacman1-x pacman1-y direction1 (.getWidth panel) (.getHeight panel))
                              (move-pacman pacman2-x pacman2-y direction2 (.getWidth panel) (.getHeight panel))
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
