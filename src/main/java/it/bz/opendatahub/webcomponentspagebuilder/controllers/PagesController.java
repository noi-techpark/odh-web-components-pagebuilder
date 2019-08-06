package it.bz.opendatahub.webcomponentspagebuilder.controllers;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import freemarker.template.TemplateException;
import it.bz.opendatahub.webcomponentspagebuilder.data.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;

@RestController
public class PagesController {

	@Autowired
	PageRepository pageRepo;

	@Autowired
	PageRenderer pageRenderer;

	@RequestMapping("/editor/{uuid}.html")
	public ResponseEntity<String> getPage(@PathVariable("uuid") String uuid) {
		Optional<Page> page = pageRepo.findById(UUID.fromString(uuid));

		if (page.isPresent()) {
			try {
				String html = pageRenderer.renderPage(page.get());
				html = html.replaceAll("</head>",
						"<link rel=\"stylesheet\" type=\"text/css\" href=\"/frontend/styles/page-editor-frame.css\">\n</head>");

				return ResponseEntity.ok(html);
			} catch (IOException | TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping("/preview/{hash}")
	public ResponseEntity<String> getPreview(@PathVariable("hash") String hash) {
		Optional<Page> page = pageRepo.findByHash(hash);

		if (page.isPresent()) {
			try {
				return ResponseEntity.ok(pageRenderer.renderPage(page.get()));
			} catch (IOException | TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}
