(ns ^:figwheel-always prais2.content)

(defrecord Header [title sortable shown width height])

(defrecord Row [h-name h-code n-ops n-deaths n-survivors survival-rate outer-low inner-low inner-high outer-high observed])

(def table1-data
  [(Row. (Header. "Hospital"                    true       true    200   50)
         (Header. "Hospital Code"               false       true    77   50)
         (Header. "Number of Operations"        true       true     95   50)
         (Header. "Number of Deaths"            false       true    75   50)
         (Header. "Number of Survivors"         false       true    85   50)
         (Header. "Observed Survival Rate %"    false      true     86   50)
         (Header. "Outer Low"                   false      false   130   50)
         (Header. "Inner Low"                   false      false   130   50)
         (Header. "Inner High"                  false      false   130   50)
         (Header. "Outer High"                  false      false   130   50)
         (Header. "Predicted & observed survival rate"  false      false   0   100)
         )

   (Row. "Belfast, Royal Victoria Hospital"	                "RVB"	204	2	202	99.0 	95.1	96.6  100     100.0 nil)
   (Row. "London, Harley Street Clinic"	                        "HSC"	482	7	475	98.5	94.8	95.9  98.8    99.4 nil)
   (Row. "Leicester, Glenfield Hospital"	                "GRL"	582	11	571	98.1	95.4	96.2  98.8    99.3 nil)
   (Row. "Newcastle, Freeman Hospital"	                        "FRE"	678	15	663	97.8	95.1	96    98.4    99.0 nil)
   (Row. "Glasgow, Royal Hospital for Children"	                "RHS"	787	28	759	96.4	95.7	96.3  98.5    99.0 nil)
   (Row. "Bristol Royal Hospital for Children"	                "BRC"	835	19	816	97.7	96.0	96.8  98.7    99.2 nil)
   (Row. "Southampton, Wessex Cardiothoracic Centre"	        "SGH"	890	17	873	98.1	95.5	96.2  98.3    98.8 nil)
   (Row.  "Leeds General Infirmary"	                        "LGI"	976	23	953	97.6	96.5	97.1  98.9    99.2 nil)
   (Row.  "Dublin, Our Lady's Children's Hospital"	        "OLS"	1056	23	1033	97.8	96.3	96.9  98.7    99.1 nil)
   (Row.  "London, Royal Brompton Hospital"	                "NHB"	1107	12	1095	98.9	96.5	97    98.7    99.1 nil)
   (Row.  "Liverpool, Alder Hey Hospital"	                "ACH"	1146	28	1118	97.6	95.8	96.4  98.3    98.7 nil)
   (Row. "London, Evelina London Children's Hospital"	        "GUY"	1204	39	1165	96.8	95.6	96.3  98.1    98.6 nil)
   (Row.  "Birmingham Children’s Hospital"	                "BCH"	1481	30	1451	98.0	95.3	95.9  97.7    98.1 nil)
   (Row. "London, Great Ormond Street Hospital for Children"	"GOS"	1881	30	1851	98.4    96.5	97    98.4    98.7 nil)]
  )
