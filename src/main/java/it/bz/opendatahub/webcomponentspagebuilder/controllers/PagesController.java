package it.bz.opendatahub.webcomponentspagebuilder.controllers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import freemarker.template.TemplateException;
import it.bz.opendatahub.webcomponentspagebuilder.controllers.ScreenshotsController.ScreenshotRenderingInProgressException;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageVersionRepository;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;

/**
 * REST controller that allows access to various views and representations of
 * page versions. Exposes the rendered HTML preview as well as a screenshot of a
 * page version.
 * 
 * @author danielrampanelli
 */
@RestController
public class PagesController {

	@Autowired
	PageVersionRepository versionsRepo;

	@Autowired
	ScreenshotsController screenshotController;

	@Autowired
	PageRenderer pageRenderer;

	@RequestMapping("/pages/page-editor/{uuid}.html")
	public ResponseEntity<String> editablePage(HttpServletRequest request, @PathVariable("uuid") String uuid) {
		Optional<PageVersion> pageVersion = versionsRepo.findById(UUID.fromString(uuid));

		if (pageVersion.isPresent()) {
			try {
				String html = pageRenderer.renderPage(pageVersion.get());

				html = html
						.replaceAll("</head>",
								String.format("<style type=\"text/css\">\n%s\n</style>\n</head>",
										IOUtils.toString(
												request.getServletContext()
														.getResourceAsStream("/frontend/styles/PageEditor-Frame.css"),
												Charset.defaultCharset())));

				return ResponseEntity.ok(html);
			} catch (IOException | TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping("/pages/preview/{uuid}.png")
	public ResponseEntity<byte[]> previewPageAsScreenshot(@PathVariable("uuid") String uuid) {
		Optional<PageVersion> pageVersion = versionsRepo.findById(UUID.fromString(uuid));

		if (pageVersion.isPresent()) {
			int attempts = 0;

			while (attempts++ <= 120) {
				try {
					byte[] screenshot = screenshotController.get(pageVersion.get());

					if (screenshot == null) {
						return new ResponseEntity<>(HttpStatus.NO_CONTENT);
					}

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.IMAGE_PNG);

					return new ResponseEntity<>(screenshot, headers, HttpStatus.OK);
				} catch (ScreenshotRenderingInProgressException e) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ee) {
						// noop
					}
				}
			}
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping("/pages/preview/{hashOrUuid}")
	public ResponseEntity<String> previewPage(@PathVariable("hashOrUuid") String hashOrUuid) {
		Optional<PageVersion> pageVersion = versionsRepo.findByHash(hashOrUuid);

		if (!pageVersion.isPresent()) {
			pageVersion = versionsRepo.findById(UUID.fromString(hashOrUuid));
		}

		if (pageVersion.isPresent()) {
			try {
				return ResponseEntity.ok(pageRenderer.renderPage(pageVersion.get()));
			} catch (IOException | TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}
