SELECT  DISTINCT  
        Recaudacion."FolioRecaudacion"
        ,Recaudacion."IdRecaudacionCliente"
        ,Recaudacion."IdRecaudacionEmpleador"
        ,COALESCE(Contrato."IdPortafolio",0) AS "IdPortafolio"
        ,COALESCE(Contrato."IdContratoSAAPV",0) AS "IdContratoSAAPV"
        ,CASE 
            WHEN Contrato."IdMedioRecaudacionFormaPago" = 1 THEN 
                CASE 
                    WHEN Recaudacion."RutEmpleador" IS NULL or Recaudacion."RutEmpleador" = '' THEN 'OK' 
                    ELSE 'NOK'
                END
            WHEN Contrato."IdMedioRecaudacionFormaPago" = 2 THEN 
                CASE 
                    WHEN Recaudacion."RutEmpleador" = Contrato."RutEmpleador" THEN 'OK' 
                    ELSE 'NOK'
                END
            ELSE 'OK'
         END AS "Validacion_Empleador"
        ,CASE WHEN Contrato."Pago" != 'TEM' THEN 'OK'
              WHEN Contrato."Pago" = 'TEM' AND Contrato."PeriodoTermino" <= :PeriodoSistema THEN 'OK'
              ELSE 'NOK' 
         END AS "Validacion_FechaTermino"
         --Validacion Forma Pago
         ,COALESCE(CASE 
            WHEN 
                Recaudacion."RecMonto" = Contrato."MontoPesos" AND (Contrato."OrigenAhorro" in ('APV','BF') OR Contrato."OrigenAhorro" = 'DC') 
            THEN 'OK'
         END, 'NOK' )AS "Validacion_Monto"
         ,CASE  WHEN    Fondo."RestringeAporte" ='S' THEN 'NOK' ELSE 'OK'    END AS "Validacion_FondoRestringido"

         
        ,Recaudacion."TipoRegimen" AS "TipoRegimen"
        ,Recaudacion."RutEmpleador" AS "RutEmpleadorRecaudacion"
        ,Contrato."RutEmpleador" AS "RutEmpleadorContrato"
        ,Contrato."Pago"
        ,Contrato."PeriodoTermino"
        ,Contrato."FormaDePago"
        --MONTOS
        ,Recaudacion."RecMonto"
        ,Contrato."MontoPesos"
        ,Contrato."RegimenTributario"
        ,COALESCE(Contrato."IdEstadoPortafolio",'BLO') AS "IdEstadoPortafolio"
        ,Cliente."IdEstado" AS "IdEstadoCliente"
        ,Contrato."IdEstadoNuevo" AS "IdEstadoContrato"
        ,CASE WHEN Asignacion."IdContratoSAAPV" = Contrato."IdContratoSAAPV" THEN 'OK' ELSE 'NOK' END  AS "Asignado"
        ,Contrato."OrigenAhorro"
        --,Recaudacion."IdCliente"
        --,Contrato."IdCliente"
        --,* 
 FROM "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" Recaudacion 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente   ON Recaudacion."IdCliente" = Cliente."IdCliente"
 LEFT JOIN  "BSCL.APV.BD::REC.AsignacionContratoRecaudacion" Asignacion ON Recaudacion."IdRecaudacionCliente" = Asignacion."IdRecaudacionCliente"
LEFT JOIN    "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" Contrato ON Contrato."IdContratoSAAPV" = Asignacion."IdContratoSAAPV" AND Recaudacion."Regimen" = Contrato."RegimenTributario"
LEFT JOIN "BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion" ConsDis ON Contrato."IdContratoSAAPV" = ConsDis."IdContratoSAAPV" 
LEFT JOIN  "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" Fondo ON  Fondo."IdSerieFondo" = ConsDis."Nemotecnico"
       
 WHERE 
     Recaudacion."IdCliente" != 0
   AND Contrato."PeriodoTermino" IS NOT NULL
   AND Asignacion."IdRecaudacionCliente" IN (:IdsRecaudacionCliente)