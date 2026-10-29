-- Actualizar Empleado aplicando cambios desde SolicitudCambio
-- Migración de ConfirmarModificarEmpleadoDatosGeneralesMasivo.sql
-- 13 Campos: IdEstado, CodigoEmpleado, CodigoTipoCargoEmpleado, IdEmpleadoSupervisor, IdSucursal, 
--           IdDireccionElectronica, UtilizaPlantillaEmpleado, IdPlantillaEmpleado, FechaInicioCargo,
--           FechaTerminoCargo, DetalleCargo, CargoIndefinido, RestringeCarteraCliente, RestringeOrdenCliente
UPDATE "BSCL.SocioNegocio.BD::EMP.Empleado" AS TGT
SET 
    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),
    "CodigoEmpleado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoEmpleado'),
        TGT."CodigoEmpleado"),
    "CodigoTipoCargoEmpleado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoTipoCargoEmpleado'),
        TGT."CodigoTipoCargoEmpleado"),
    "IdEmpleadoSupervisor" = COALESCE(
        (SELECT CASE WHEN MAX("ValorNuevo") = '' THEN NULL ELSE MAX("ValorNuevo") END 
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEmpleadoSupervisor'),
        TGT."IdEmpleadoSupervisor"),
    "IdSucursal" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdSucursal'),
        TGT."IdSucursal"),
    "IdDireccionElectronica" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdDireccionElectronica'),
        TGT."IdDireccionElectronica"),
    "UtilizaPlantillaEmpleado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'UtilizaPlantillaEmpleado'),
        TGT."UtilizaPlantillaEmpleado"),
    "IdPlantillaEmpleado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdPlantillaEmpleado'),
        TGT."IdPlantillaEmpleado"),
    "FechaInicioCargo" = COALESCE(
        (SELECT TO_DATE(MAX("ValorNuevo")) FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'FechaInicioCargo'),
        TGT."FechaInicioCargo"),
    "FechaTerminoCargo" = COALESCE(
        (SELECT TO_DATE(MAX("ValorNuevo")) FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'FechaTerminoCargo'),
        TGT."FechaTerminoCargo"),
    "DetalleCargo" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'DetalleCargo'),
        TGT."DetalleCargo"),
    "CargoIndefinido" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CargoIndefinido'),
        TGT."CargoIndefinido"),
    "RestringeCarteraCliente" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'RestringeCarteraCliente'),
        TGT."RestringeCarteraCliente"),
    "RestringeOrdenCliente" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
                ELSE 'EMPLEADO'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'RestringeOrdenCliente'),
        TGT."RestringeOrdenCliente"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdEmpleado" = :IdEmpleado
