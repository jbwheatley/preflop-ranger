import {themes as prismThemes} from 'prism-react-renderer';

const config = {
  title: 'Preflop Ranger',
  tagline: 'View, edit, and share preflop ranges for Texas Hold\'em',
  favicon: 'img/favicon.ico',

  url: 'https://jbhweatley.github.io',
  // change to / for local dev (cd website; npm start)
  baseUrl: '/preflop-ranger/',
  organizationName: 'io.github.jbwheatley',
  projectName: 'preflop-ranger',
  trailingSlash: false,

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      ({
        docs: {
          sidebarPath: './sidebars.js',
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],


  themeConfig:
    ({
      image: 'img/ranger.png',
      metadata: [
        {name: 'keywords', content: 'poker, preflop, preflop chart, poker training, free, software, desktop, preflop range, poker range, holdem'},
      ],
      navbar: {
        title: 'Preflop Ranger',
        logo: {
          alt: 'ranger-logo',
          src: 'img/ranger.png',
        },
        items: [
          {
            to: '/download',
            position: 'left',
            label: 'Download',
          },
//          {
//            type: 'docSidebar',
//            sidebarId: 'tutorialSidebar',
//            position: 'left',
//            label: 'Guide',
//          },
        ],
      },
      footer: {
        style: 'dark',
        logo: {
                  alt: 'ranger-logo',
                  src: 'img/ranger.png',
                  width: 50,
                },
        links: [
//          {
//            title: 'Docs',
//            items: [
//              {
//                label: 'Guide',
//                to: '/docs/intro',
//              },
//            ],
//          },
          {
            title: 'Community',
            items: [
              {
                label: 'Github',
                href: 'https://github.com/jbwheatley/preflop-ranger/discussions',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Source code',
                href: 'https://github.com/jbwheatley/preflop-ranger',
              },
              {
                label: 'License',
                href: 'https://www.gnu.org/licenses/gpl-3.0',
              },
              {
                label: 'Sponsor',
                href: 'https://buymeacoffee.com/jbwheatley',
              },
            ],
          },
        ],
        copyright: `io.github.jbwheatley, ${new Date().getFullYear()}`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;
