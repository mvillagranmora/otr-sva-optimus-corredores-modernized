select

       RUT_CORR, -- ducumento identidad corredora

       RAZON_SOCIAL, --nombre persona juridica

       NEMO, --nombre corto en la tabla contraparte o corredora?

       COD_CORR, -- codigo bcs en la tabla corredora

       COD_CORR_SVS, --codigo regulador

       COD_CORR_VALPO, --codigo corredora BCV

       COD_CORR_BEC --codigo corredora BEC

from tbutusua
where tipo_ent = '08' and estado = 'V' and cod_corr < 100 
and rut_corr NOT IN (' 90249000-0','         -0') 
and rut_corr NOT IN (' 96572920-8', '         -0')
ORDER BY COD_CORR_SVS ASC;