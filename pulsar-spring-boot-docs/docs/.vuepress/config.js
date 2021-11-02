module.exports = {
    title: 'Pulsar Spring Boot Starter',
    themeConfig: {
        home:false,
        search: true,
        logo:'/images/logo.png',
        searchMaxSuggestions: 10,
        lastUpdated: 'Last Updated',
        nav: [
            {text: 'About', link: '/'},
            {text: 'Github', link: 'https://github.com/pulsar-eco/spring-boot-starter-pulsar'}
        ],
        sidebar: [
            {
                title: 'Guide',
                collapsable: false, // 可选的, 默认值是 true,
                children: ['/','/guide/gettingStarted/', '/guide/installation/']
            },
            {
                title: 'Advanced',
                collapsable: false, // 可选的, 默认值是 true,
                children: ['/advanced/consumer/', '/advanced/producer/', '/advanced/other/']
            },
            {
                title: 'About',
                collapsable: false, // 可选的, 默认值是 true,
                children: ['/about/author/']
            }
        ]
    }
};