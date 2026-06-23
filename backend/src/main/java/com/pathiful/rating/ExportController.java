package com.pathiful.rating;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für den Export von Routen.
 *
 * GET /api/routes/{routeId}/export/kml  – KML-Export
 * GET /api/routes/{routeId}/export/gpx  – GPX-Export
 */
@RestController
@RequestMapping("/api/routes/{routeId}/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * GET /api/routes/{routeId}/export/kml
     *
     * Exportiert eine Route als KML-Datei.
     */
    @GetMapping("/kml")
    public ResponseEntity<String> exportKml(@PathVariable Long routeId) {
        String kml = exportService.exportAsKml(routeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"route-" + routeId + ".kml\"");

        return ResponseEntity.ok().headers(headers).body(kml);
    }

    /**
     * GET /api/routes/{routeId}/export/gpx
     *
     * Exportiert eine Route als GPX-Datei.
     */
    @GetMapping("/gpx")
    public ResponseEntity<String> exportGpx(@PathVariable Long routeId) {
        String gpx = exportService.exportAsGpx(routeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"route-" + routeId + ".gpx\"");

        return ResponseEntity.ok().headers(headers).body(gpx);
    }
}
