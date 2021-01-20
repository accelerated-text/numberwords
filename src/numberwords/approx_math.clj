(ns numberwords.approx-math)

(defn nat-num? [x] (not (neg? x)))

(defn not-inf? [x]
  (and
   (not (Double/isInfinite x))
   (not (Double/isNaN x))))

(defn delta [x y] (Math/abs (- x (* 1.0 y))))

(defmulti bounding-box
  "Find a bounding box around the actual-value, where the edges
  of the box represent actual-value's rounded smaller and bigger numbers"
  (fn [actual-value scale] [(class actual-value) (class scale)]))

(defmethod bounding-box [java.lang.Long java.lang.Long] [actual-value scale]
  (let [m (mod actual-value scale)]
    [(- actual-value m)
     (- (+ actual-value scale) m)]))

(defmethod bounding-box :default [actual-value scale]
  (let [rational-av (rationalize actual-value)
        m (mod  rational-av scale)]
    [(rationalize (- rational-av m))
     (rationalize (- (+ rational-av scale) m))]))

(defn distances-from-edges
  [actual-value scale]
  (let [[start end] (bounding-box actual-value scale)]
    [[start (delta start actual-value)]
     [end (delta end actual-value)]]))

(defn unreliable?
  "Actual value and scale values which will generate unreliable results:
  - actual-value much bigger that the scale
  - scale is bigger than the actual value"
  [actual-value scale]
  ;; 1000x difference between the scale and value is a random choice
  (or (< 1000 (/ actual-value scale))
      #_(and (< 1 scale) (< actual-value scale))))
