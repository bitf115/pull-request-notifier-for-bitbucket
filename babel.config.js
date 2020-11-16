module.exports = api => {
    const isTest = api.env('test');
    const testOnly = (entry, defaultValue = {}) => (isTest ? entry : defaultValue);
    return {
        presets: [
            ['@babel/preset-env', {modules: testOnly('commonjs', false)}],
            '@babel/preset-typescript',
            '@babel/preset-react',
        ],
        plugins: ['@babel/transform-runtime',
            '@babel/plugin-syntax-dynamic-import',
            '@babel/plugin-proposal-class-properties',],
    };
};
