(ns pacman.core
  (:require [clojure.java.io :as io]
            [clojure.set :refer :all])
  (:import (javax.swing JFrame JPanel Timer)
           (java.awt Color)
           (java.awt.image BufferedImage)
           (java.awt.event ActionListener KeyEvent KeyListener)
           (javax.imageio ImageIO)
           (java.io File)
           (java.lang Thread)))

(def pacman-size 20)
(def open-mouth 270)
(def closed-mouth 360)
(def angle (atom closed-mouth))
(def direction (atom :right-open))
(def images (atom {}))
(def pacman-x (atom 0)) ; Coordenada x inicial de Pacman
(def pacman-y (atom 0)) ; Coordenada y inicial de Pacman
(def move-step 5) ; Cantidad de píxeles que Pacman se mueve en cada paso

;; Configuracion de la bomba
(def bomb-size 20)
(def bomb-position (atom {:x 200 :y 200}))
(def bomb-visible (atom false))
(def explosion-time (atom nil))
(def max-explosion-size 100)

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

;; (defn save-image [image file-path format]
;;   (try
;;     (ImageIO/write image format (File. file-path))
;;     (println "Image saved successfully:" file-path)
;;     true
;;     (catch Exception e
;;       (println "Exception while saving image:" file-path (.getMessage e))
;;       false)))

(defn draw-image [g x y sizex sizey image]
  (.drawImage g image x y sizex sizey nil))

(defn get-current-image []
  (let [key (if (= @angle closed-mouth) :closed @direction)]
    (get @images key)))

(defn collision-with-explosion? []
  (when (and @explosion-time (not @bomb-visible))
    (let [explosion-center-x (:x @bomb-position)
          explosion-center-y (:y @bomb-position)
          pacman-center-x (+ @pacman-x (/ pacman-size 2))
          pacman-center-y (+ @pacman-y (/ pacman-size 2))
          dx (- explosion-center-x pacman-center-x)
          dy (- explosion-center-y pacman-center-y)
          distance (Math/sqrt (+ (* dx dx) (* dy dy)))]
      (< distance (+ (/ max-explosion-size 2) (/ pacman-size 2))))))

(defn move-pacman [panel-width panel-height]
  (cond
    (= @direction :up-open) (swap! pacman-y #(max 0 (- % move-step)))
    (= @direction :down-open) (swap! pacman-y #(min (- panel-height pacman-size) (+ % move-step)))
    (= @direction :left-open) (swap! pacman-x #(max 0 (- % move-step)))
    (= @direction :right-open) (swap! pacman-x #(min (- panel-width pacman-size) (+ % move-step))))
  (when (collision-with-explosion?)
    (reset! pacman-x 0)
    (reset! pacman-y 0)))

(defn create-pacman-panel []
  (proxy [JPanel ActionListener KeyListener] []
    (keyPressed [e]
      (cond
        (= (.getKeyCode e) KeyEvent/VK_W) (reset! direction :up-open)
        (= (.getKeyCode e) KeyEvent/VK_S) (reset! direction :down-open)
        (= (.getKeyCode e) KeyEvent/VK_D) (reset! direction :right-open)
        (= (.getKeyCode e) KeyEvent/VK_A) (reset! direction :left-open)
        (and (= (.getKeyCode e) KeyEvent/VK_SPACE) (not @bomb-visible) (nil? @explosion-time))
        (do
          (reset! bomb-position {:x @pacman-x :y @pacman-y})
          (reset! bomb-visible true);; Detener el timer anterior si existe
          (future
            (Thread/sleep 2000)
            (reset! bomb-visible false)
            (reset! explosion-time 1000)))))

    (keyReleased [e])
    (keyTyped [e])
    (paintComponent [g]
      (proxy-super paintComponent g)
      (let [pacman-image (get-current-image)
            bomb-image (get @images :bomb)]
        (when pacman-image
          (draw-image g @pacman-x @pacman-y pacman-size pacman-size pacman-image))
        (when @bomb-visible
          (draw-image g (:x @bomb-position) (:y @bomb-position) bomb-size bomb-size bomb-image))
        (when (and @explosion-time (not @bomb-visible))
          (let [elapsed-time (- (System/currentTimeMillis) @explosion-time)
                current-explosion-size (min elapsed-time max-explosion-size)
                explosion-x (:x @bomb-position)
                explosion-y (:y @bomb-position)]
            (.setColor g Color/RED)
            (.fillOval g (- explosion-x (/ current-explosion-size 2))
                       (- explosion-y (/ current-explosion-size 2))
                       current-explosion-size current-explosion-size)
            (reset! explosion-time nil)))
        (when (collision-with-explosion?)
          (reset! pacman-x 0)
          (reset! pacman-y 0))))))

(defn create-window []
  (let [frame (JFrame. "Pacman")
        panel (create-pacman-panel)
        timer (Timer. 100 (reify ActionListener
                            (actionPerformed [_ _]
                              (move-pacman (.getWidth panel) (.getHeight panel))
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
                  :bomb (load-image "resources/imgs/Bomba.png")})
  (doseq [[key image] @images]
    (if image
      (println (str "Image for key " key " is loaded successfully."))
      (println (str "Image for key " key " is not loaded.")))))

(defn -main [& args]
  (load-images)
  (create-window))