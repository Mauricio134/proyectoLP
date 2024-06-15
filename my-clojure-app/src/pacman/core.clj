(ns pacman.core
  (:require [clojure.java.io :as io]
            [clojure.set :refer :all])
  (:import (javax.swing JFrame JPanel Timer)
           (java.awt Color)
           (java.awt.image BufferedImage)
           (java.awt.event ActionListener KeyEvent KeyListener)
           (javax.imageio ImageIO)
           (java.io File)))

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

(def move-step 5) ; Cantidad de píxeles que Pacman se mueve en cada paso

(def images (atom {}))

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

(defn save-image [image file-path format]
  (try
    (ImageIO/write image format (File. file-path))
    (println "Image saved successfully:" file-path)
    true
    (catch Exception e
      (println "Exception while saving image:" file-path (.getMessage e))
      false)))

(defn draw-pacman [g x y image]
  (.drawImage g image x y pacman-size pacman-size nil))

(defn get-current-image [direction]
  (let [key (if (= @angle closed-mouth)
              (if (.endsWith (name @direction) "2") :closed2 :closed)
              @direction)]
    (get @images key))) ; fallback to :closed if key not found

(defn move-pacman [pacman-x pacman-y direction panel-width panel-height]
  (cond
    (= @direction :up-open) (swap! pacman-y #(max 0 (- % move-step)))
    (= @direction :down-open) (swap! pacman-y #(min (- panel-height pacman-size) (+ % move-step)))
    (= @direction :left-open) (swap! pacman-x #(max 0 (- % move-step)))
    (= @direction :right-open) (swap! pacman-x #(min (- panel-width pacman-size) (+ % move-step)))
    (= @direction :up-open2) (swap! pacman-y #(max 0 (- % move-step)))
    (= @direction :down-open2) (swap! pacman-y #(min (- panel-height pacman-size) (+ % move-step)))
    (= @direction :left-open2) (swap! pacman-x #(max 0 (- % move-step)))
    (= @direction :right-open2) (swap! pacman-x #(min (- panel-width pacman-size) (+ % move-step)))))

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
        (= (.getKeyCode e) KeyEvent/VK_LEFT) (reset! direction2 :left-open2)))
    (keyReleased [e])
    (keyTyped [e])
    (paintComponent [g]
      (proxy-super paintComponent g)
      (let [image1 (get-current-image direction1)
            image2 (get-current-image direction2)]
        (when image1
          (draw-pacman g @pacman1-x @pacman1-y image1))
        (when image2
          (draw-pacman g @pacman2-x @pacman2-y image2))))))

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
                  :right-open2 (load-image "resources/imgs/pacman2-right-open.png")})
  (println "Images loaded:" @images)
  (doseq [[key image] @images]
    (if image
      (println (str "Image for key " key " is loaded successfully."))
      (println (str "Image for key " key " is not loaded.")))))

(defn -main [& args]
  (println "Current working directory:" (System/getProperty "user.dir"))
  (load-images)
  (create-window))
