(ns pacman.core
  (:require [clojure.java.io :as io]
            [clojure.set :refer :all])
  (:import (javax.swing JFrame JPanel Timer)
           (java.awt Color)
           (java.awt.image BufferedImage)
           (java.awt.event ActionListener KeyEvent KeyListener)
           (javax.imageio ImageIO)
           (java.io File)))

(def pacman-size 50)
(def open-mouth 270)
(def closed-mouth 360)
(def angle (atom closed-mouth))
(def direction (atom :right-open))
(def images (atom {}))

;; Caracteriristicas de la bomba
(def bomb-size 50)
(def bomb-position {:x 200 :y 200})
(def bomb-visible (atom false))

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

(defn draw-pacman [g width height image]
  (let [x (- (/ width 2) (/ pacman-size 2))
        y (- (/ height 2) (/ pacman-size 2))]
    (.drawImage g image x y pacman-size pacman-size nil)))

(defn draw-image [g x y size image]
  (.drawImage g image x y size size nil))

(defn get-current-image []
  (let [key (if (= @angle closed-mouth) :closed @direction)]
    (get @images key))) ; fallback to :closed if key not found

(defn create-pacman-panel []
  (proxy [JPanel ActionListener KeyListener] []
    (keyPressed [e]
      (cond
        (= (.getKeyCode e) KeyEvent/VK_W) (reset! direction :up-open)
        (= (.getKeyCode e) KeyEvent/VK_S) (reset! direction :down-open)
        (= (.getKeyCode e) KeyEvent/VK_D) (reset! direction :right-open)
        (= (.getKeyCode e) KeyEvent/VK_A) (reset! direction :left-open)))
    (keyReleased [e])
    (keyTyped [e])
    (paintComponent [g]
      (proxy-super paintComponent g)
      (let [pacman-image (get-current-image)
            bomb-image (get @images :bomb)]
        (when pacman-image
          (draw-pacman g (.getWidth this) (.getHeight this) pacman-image))
        (when bomb-image
          (draw-image g (:x bomb-position) (:y bomb-position) bomb-size bomb-image))))))

(defn create-window []
  (let [frame (JFrame. "Pacman")
        panel (create-pacman-panel)
        timer (Timer. 100 (reify ActionListener
                            (actionPerformed [_ _]
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
  (println "Images loaded:" @images)
  (doseq [[key image] @images]
    (if image
      (println (str "Image for key " key " is loaded successfully."))
      (println (str "Image for key " key " is not loaded.")))))

(defn -main [& args]
  (println "Current working directory:" (System/getProperty "user.dir"))
  (load-images)
  (create-window))
