<!doctype>
<html>
  <head>
    <@metatags></@metatags>
    <link rel="icon" type="image/png" href="images/favicon.png"/>
    <style type="text/css" data-font="odh-kievit@400">
      @font-face {
        font-display: swap;
        font-family: 'odh-kievit';
        font-style: normal;
        font-weight: 400;
        src: url('fonts/odh-kievit.woff') format('woff');
      }
    </style>
    <style type="text/css">
      @import url('https://unpkg.com/normalize.css@8.0.1/normalize.css');

      body {
        background-color: #ffffff;
        display: block;
        position: relative;
      }

      header nav {
        background-color: rgba(0, 0, 0, 0.5);
        -webkit-box-sizing: border-box;
                box-sizing: border-box;
        left: 0;
        opacity: 0;
        padding: 16px;
        position: fixed;
        right: 0;
        -webkit-transition: all .25s ease-in-out;
        -o-transition: all .25s ease-in-out;
        transition: all .25s ease-in-out;
        top: 0;
        width: 100%;
        z-index: 5000;
      }

      header nav img {
        display: block;
        height: 24px;
        margin: 0 26px 0 auto;
      }

      @media screen and (min-width: 992px) {
        header nav img {
          height: 42px;
          margin: 0 92px 0 auto;
        }
      }

      header #logo {
        display: block;
        position: absolute;
        right: 32px;
        top: 0;
        z-index: 3000;
      }

      header #logo img {
        height: 64px;
      }

      @media screen and (min-width: 768px) {
        header #logo img {
          height: 96px;
        }
      }

      @media screen and (min-width: 992px) {
        header #logo {
          right: 96px;
        }

        header #logo img {
          height: 160px;
        }
      }

      header.has-been-scrolled nav {
        opacity: 1;
      }

      footer {
        display: block;
      }

      footer nav {
        font-family: 'odh-kievit', sans-serif;
        font-size: 17px;
        margin: 0 auto;
        max-width: 750px;
        padding: 16px 0;
      }

      footer nav ul {
        margin: 0;
        padding: 24px 16px;
      }

      footer nav ul li {
        color: #758592;
        display: block;
        list-style-type: none;
        padding: 6px;
        text-align: center;
      }

      footer nav ul li a {
        color: #758592;
        text-decoration: none;
      }

      footer nav ul li a:hover {
        text-decoration: underline;
      }

      footer img {
        vertical-align: bottom;
        width: 100%;
      }

      @media screen and (min-width: 768px) {
        footer nav {
          max-width: 750px;
        }

        footer nav ul li {
          display: inline-block;
          padding: 0 6px;
          text-align: left;
        }
      }

      @media screen and (min-width: 992px) {
        footer nav {
          max-width: 970px;
        }
      }

      @media screen and (min-width: 1240px) {
        footer nav {
          max-width: 1170px;
        }
      }
    </style>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script type="text/javascript">
      $(function() {
        function updateHeader() {
          if ($(document).scrollTop() < $('header #logo').height()) {
            $('header').removeClass('has-been-scrolled');
          } else {
            $('header').addClass('has-been-scrolled');
          }
        }

        $(document).on('scroll', function() {
          updateHeader();
        });

        $(document).on('resize', function() {
          updateHeader();
        });

        updateHeader();
      });
    </script>
  </head>
  <body>
    <header>
      <nav id="sitebar">
        <a href="https://www.suedtirol.info" target="_blank">
          <img src="images/logo-sticky.png"/>
        </a>
      </nav>

      <a id="logo" href="https://www.suedtirol.info" target="_blank">
        <img src="images/logo-default.svg"/>
      </a>
    </header>
    <main>
      <@contents></@contents>
    </main>
    <footer>
      <nav>
        <ul>
          <li>&copy; IDM S&uuml;dtirol 2020</li>
          <li><a href="https://lp.suedtirol.info/footer/IDM_Privacy_Cookies_Policy_DE.pdf" target="_blank">Privacy &amp; Cookies</a></li>
          <li><a href="https://lp.suedtirol.info/de/impressum-2020" target="_blank">Impressum</a></li>
        </ul>
      </nav>
      <img src="images/footer.svg"/>
    </footer>
    <@widgets></@widgets>
  </body>
</html>