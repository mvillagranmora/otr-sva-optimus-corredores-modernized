SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.NotificarEstadoSpot";
-- 
--  Proposito: Servicio para la notificacion del estado de operacion spot
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 09-05-2018
--
GO  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.NotificarEstadoSpot" (
    -- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                 bigint,         --ID Empresa destino
    IN  p_IdUsuario                 bigint,         --ID del usuario que solicita el servicio
    IN  p_IdModulo                  NVARCHAR(3),    --ID del Modulo que lo invoca
    --IN  p_IdPortafolio                bigint,

    -- Parametros de entrada para la creacion del usuario
    IN  p_IdOperacionesSpot                 TABLE("IdOperacionSpot" BIGINT)

) 
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL"
    AS
BEGIN
    -----------------------------------------------------
    --Declaraciones Variables
    -----------------------------------------------------
    DECLARE v_IdPersonaRemitente          BIGINT;
    DECLARE v_IdDireccionPersonaRemitente BIGINT;
    DECLARE v_FechaSistema                NVARCHAR(10);
    DECLARE v_hora                        NVARCHAR(10);
    DECLARE p_IdCartera                   BIGINT;
    
    -----------------------------------------------------
    --Validaciones básicas (Codigos,Existencias,etc)
    -----------------------------------------------------
    BEGIN
        
    END;
    --------------------
    --Negocio Aqui
    -------------------
    BEGIN
        SELECT CURRENT_TIME into v_hora from DUMMY;
        CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
       
        /***/
        -- remitente
        SELECT CE."IdPersona", CE."IdDireccionElectronica"  INTO v_IdPersonaRemitente, v_IdDireccionPersonaRemitente
        FROM "BSCL.Plataforma.BD::ACC.Usuario" U
        INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"
        WHERE "IdUsuario" = :p_IdUsuario;
    
        
        t_cliente = 
            SELECT
                --CABECERA
                IDSPT."IdOperacionSpot" AS "IdSecuencia",
                CCL."NombreCompleto" AS "Nombre",
                CCL."DocumentoIdentidad" AS "DocumentoIdentidad",
                TO_NVARCHAR(OSP."IdPortafolio") AS "CodigoPortafolio",
                OSP."IdPortafolio",
                COALESCE(CPT."Numero", '') AS "Telefono",
                COALESCE(CPDE."Direccion", '') AS "Correo",
                CEMP."NombreCompleto" AS "Agente",
                TO_VARCHAR(TO_DATE(v_FechaSistema, 'YYYY-MM-DD'), 'DD-MM-YYYY') AS "Fecha",
                TO_VARCHAR(v_hora) AS "Hora",
                --DETALLE
                OSP."IdOperacion" AS "Folio",
                OSP."TipoOperacion",
                OSP."ParidadCosto" AS "Paridad",
                OSP."MontoMonedaPrincipal" AS "MontoRecibir",
                OSP."TipoCambioCierre" AS "TipoCambio",
                OSP."MontoMonedaSecundaria" AS "MontoEntregar",
                OSP."Estado",
                --PIE
                CCL."IdDireccionElectronicaPrincipal",
                CCL."IdPersona",
                CE."Nombre" AS "Corredora",
                '' AS "CallCenter"
            FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" OSP
            INNER JOIN :p_IdOperacionesSpot IDSPT ON OSP."IdOperacion" = IDSPT."IdOperacionSpot"
            INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON OSP."IdPortafolio" = PORT."IdPortafolio"
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL ON CCL."IdCliente" = PORT."IdCliente"
            LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CCL."IdTelefonoPrincipal" = CPT."IdTelefono"
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON CCL."IdEmpleadoEjecutivo" = CEMP."IdEmpleado"
            LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CE ON CE."IdEmpresa" = :p_IdEmpresa
            WHERE OSP."IdEmpresa" = p_IdEmpresa;
            
            --SELECT * FROM "BSCL.Mesa.BD::SPT.OperacionSpot"
            --SELECT * FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
        --construir notificacion
        p_Eventos = SELECT 
        'CAMBIO_ESTADO_OPERACION_SPOT' AS "CodigoTipoEvento", 
        "IdSecuencia", 
        CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
        :v_IdPersonaRemitente AS "IdPersonaRemitente", 
        "IdPersona" AS "IdPersonaDestinatario",
        "IdPortafolio"
        FROM :t_cliente;
        
        p_IdDirecciones = SELECT 
            E."IdSecuencia",
            C."IdDireccionElectronicaPrincipal" AS "IdDireccion",
            'TO' AS "TipoDestinatario"
        FROM :p_Eventos E
        INNER JOIN :t_cliente C ON E."IdSecuencia" = C."IdSecuencia"
        UNION ALL
            SELECT 
                E."IdSecuencia",
                v_IdDireccionPersonaRemitente AS "IdDireccion",
                'CC' AS "TipoDestinatario"
            FROM :p_Eventos E;
        
        p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
        
        
        p_EventoDetalle = SELECT "IdSecuencia", "Llave", "Valor"
        FROM (
            SELECT * ,
                    
            COALESCE(
                "1" || "IdSecuencia",
                "2" || "Nombre",
                "3" || "DocumentoIdentidad",
                "4" || "CodigoPortafolio",
                "5" || "IdPortafolio",
                "6" || "Telefono",
                "7" || "Correo",
                "8" || "Agente",
                "9" || "Fecha",
                "10" || "Hora",
                "11" || "Folio",
                "12" || "TipoOperacion",
                "13" || "Paridad",
                "14" || "MontoRecibir",
                "15" || "TipoCambio",
                "16" || "MontoEntregar",
                "17" || "Estado",
                "18" || "IdDireccionElectronicaPrincipal",
                "19" || "IdPersona",
                "20" || "Corredora",
                "21" || "CallCenter"
            ) AS "Valor",
                    
            COALESCE(
                "1" || 'IdSecuencia',
                "2" || 'Nombre',
                "3" || 'DocumentoIdentidad',
                "4" || 'CodigoPortafolio',
                "5" || 'IdPortafolio',
                "6" || 'Telefono',
                "7" || 'Correo',
                "8" || 'Agente',
                "9" || 'Fecha',
                "10" || 'Hora',
                "11" || 'Folio',
                "12" || 'TipoOperacion',
                "13" || 'Paridad',
                "14" || 'MontoRecibir',
                "15" || 'TipoCambio',
                "16" || 'MontoEntregar',
                "17" || 'Estado',
                "18" || 'IdDireccionElectronicaPrincipal',
                "19" || 'IdPersona',
                "20" || 'Corredora',
                "21" || 'CallCenter'
            ) AS "Llave" 
            FROM :t_cliente T, "BSCL.Plataforma.BD::PLA.Identidad"
        )
        WHERE "Valor" IS NOT NULL;
        
        --Guardar eventos
        CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
            p_IdEmpresa => p_IdEmpresa,
            p_IdUsuario => p_IdUsuario,
            p_IdModulo => p_IdModulo,
            p_Eventos => :p_Eventos,
            p_Valores => :p_EventoDetalle,
            p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
            p_IdDirecciones => :p_IdDirecciones,
            p_out_IdEvento => p_out_IdEvento
        );
    END;
END;
/*

*/