#!/bin/bash

cd ~/clojure/prais2
lein clean
lein cljsbuild once min
gulp
#rsync -av resources/public/ gmp26@webuu1.maths.cam.ac.uk:/www/drupal/sites/understandinguncertainty.org/files/animations/standalone/PRAIS2
rsync -av resources/public/ gmp26@webuu2.maths.cam.ac.uk:/var/www/childrensheartsurgery.info/html/
# rsync -av PRAIS2s0-1/ gmp26@webuu1.maths.cam.ac.uk:/www/drupal/sites/understandinguncertainty.org/files/animations/standalone/PRAIS2s0-1
